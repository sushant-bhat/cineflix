package com.anthat.cineflix.service.impl;

import com.anthat.cineflix.config.ModuleConfig;
import com.anthat.cineflix.dto.VideoDTO;
import com.anthat.cineflix.dto.VideoStreamDTO;
import com.anthat.cineflix.dto.VideoThumbnailDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.exception.VideoUploadException;
import com.anthat.cineflix.model.Video;
import com.anthat.cineflix.repo.VideoRepo;
import com.anthat.cineflix.service.VideoService;
import com.anthat.cineflix.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {
    private static final Logger LOGGER = LogManager.getLogger(VideoServiceImpl.class);
    private final VideoRepo videoRepo;

    @Value("${app.upload.dir}")
    private String DIR;


    private String copyFileToPath(MultipartFile file, String destination, String id) throws IOException {

        // Create the upload directory if it doesn't exist
        Path uploadPath = Paths.get(DIR, destination, id);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Get the file name and create the destination file path
        String fileName = file.getOriginalFilename();
        Path destinationPath = uploadPath.resolve(fileName);

        // Copy the file to the destination
        Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        return destinationPath.toUri().getPath();
    }

    @Override
    public void uploadVideo(VideoDTO video, MultipartFile videoThumbnail, MultipartFile videoFile) throws VideoUploadException {
        try {
            Video videoEntity = Video.clone(video);
            videoEntity.setVideoId(UUID.randomUUID().toString());

            String videoId = videoEntity.getVideoId();

            // Save thumbnail in a directory and update path in video thumbnail url
            String thumbnailPath = copyFileToPath(videoThumbnail, "thumbnails", videoId);
            videoEntity.setThumbnailContentType(videoThumbnail.getContentType());
            videoEntity.setVideoThumbnailUrl(thumbnailPath);

            // Save file in a directory and update path in video url
            String videoPath = copyFileToPath(videoFile, "videos", videoId);
            videoEntity.setVideoContentType(videoFile.getContentType());
            videoEntity.setVideoUrl(videoPath);

            // Save video object into postgres
            videoRepo.save(videoEntity);

            // Now need to transcode the video and save different versions of the file using ffmpeg
            // TODO: Make this maybe async sending a message back video is accepted and being uploaded
            transcodeVideo(videoFile, videoPath, videoId);
        } catch (IOException e) {
            LOGGER.error("Error while creating folder ", e);
            throw new VideoUploadException("Something went wrong while uploading video");
        } catch (Exception e) {
            LOGGER.error("Error while uploading video", e);
            throw new VideoUploadException("Something went wrong while uploading video");
        }
    }

    private void transcodeVideo(MultipartFile videoFile, String inputPath, String videoId) throws IOException, InterruptedException {
        String[] resolutions = {"640x360", "854x480", "1280x720", "1920x1080"};
        String[] bitRates = {"500k", "800k", "1500k", "4000k"};

        Path transcodePath = Paths.get(DIR, "transcode", videoId);
        if (!Files.exists(transcodePath)) {
            Files.createDirectories(transcodePath);
        }

        // Get the file name and create the destination file path
        String fileName = StringUtils.stripFilenameExtension(videoFile.getOriginalFilename());
        Path destinationPath = transcodePath.resolve(fileName);

        for (int index = 0;index < resolutions.length;++index) {
            String outputPath = destinationPath + "_" + resolutions[index].replace("x", "_") + ".mp4";

            List<String> transcodeCmd = new ArrayList<>();
            transcodeCmd.add("ffmpeg");
            transcodeCmd.add("-i");
            transcodeCmd.add(inputPath);
            transcodeCmd.add("-vf");
            transcodeCmd.add("scale=" + resolutions[index]);
            transcodeCmd.add("-b:v");
            transcodeCmd.add(bitRates[index]);
            transcodeCmd.add("-hls_time");
            transcodeCmd.add("10");
            transcodeCmd.add("-hls_list_size");
            transcodeCmd.add("0");
            transcodeCmd.add("-f");
            transcodeCmd.add("hls");
            transcodeCmd.add(outputPath + ".m3u8");

            ProcessBuilder processBuilder = new ProcessBuilder(transcodeCmd);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Video processing failed for resolution: " + resolutions[index] + ". Exit code: " + exitCode);
            } else {
                System.out.println("Successfully processed for resolution: " + resolutions[index] + " to " + outputPath);
            }
        }
    }

    @Override
    public VideoDTO getVideoInfoById(String videoId) throws VideoAccessException {
        Video foundVideoMeta = videoRepo.findById(videoId).orElse(null);
        if (foundVideoMeta == null) {
            throw new VideoAccessException("Video not found");
        }

        return VideoDTO.clone(foundVideoMeta);
    }

    @Override
    public VideoStreamDTO getVideoStreamById(String videoId, String range) throws VideoAccessException {
        Video foundVideoMeta = videoRepo.findById(videoId).orElse(null);
        if (foundVideoMeta == null) {
            throw new VideoAccessException("Video record not found");
        }

        Path filePath = Paths.get(foundVideoMeta.getVideoUrl());
        if (!Files.exists(filePath)) {
            throw new VideoAccessException("Video file not found");
        }

        if (org.apache.commons.lang3.StringUtils.isBlank(range)) {
            return VideoStreamDTO.builder()
                    .videoResource(new FileSystemResource(filePath))
                    .build();
        }

        // Chunking starts
        File videoFile = filePath.toFile();
        long videoLength = videoFile.length();
        long start, end;

        InputStream inputStream;
        try {
            String[] ranges = range.substring("bytes=".length()).split("-");
            start = Long.parseLong(ranges[0]);
            end = Math.min(start + AppConstants.CHUNK_SIZE - 1, videoLength - 1);

            inputStream = Files.newInputStream(filePath);

            inputStream.skip(start);

            long contentLength = end - start + 1;
            byte[] data = new byte[(int) contentLength];
            int readBytes = inputStream.read(data, 0, data.length);
            LOGGER.debug("Read {} bytes", readBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes " + start + "-" + end + "/" + videoLength);
            headers.setContentLength(contentLength);

            return VideoStreamDTO.builder()
                    .videoResource(new ByteArrayResource(data))
                    .contentType(foundVideoMeta.getVideoContentType())
                    .headers(headers)
                    .build();
        } catch (NumberFormatException exp) {
            throw new VideoAccessException("Invalid range sent");
        } catch (IOException exp) {
            throw new VideoAccessException("Error while reading video file");
        }
    }

    @Override
    public VideoThumbnailDTO getVideoThumbnailById(String videoId) throws VideoAccessException {
        Video foundVideoMeta = videoRepo.findById(videoId).orElse(null);
        if (foundVideoMeta == null) {
            throw new VideoAccessException("Video record not found");
        }

        Path filePath = Paths.get(foundVideoMeta.getVideoThumbnailUrl());
        if (!Files.exists(filePath)) {
            throw new VideoAccessException("Video thumbnail not found");
        }

        return VideoThumbnailDTO.builder()
                .thumbnailResource(new FileSystemResource(foundVideoMeta.getVideoThumbnailUrl()))
                .contentType(foundVideoMeta.getThumbnailContentType())
                .build();
    }

    @Override
    public List<VideoDTO> getModuleVideos(ModuleConfig moduleConfig) {
        List<Video> videoList = new ArrayList<>();
        switch (moduleConfig.getModuleType()) {
            case HERO -> videoList = videoRepo.findById("4aac0b18-e68f-4bc4-9f18-475e17b7127d").stream().toList();
            case CONTINUE, RECOM -> videoList = videoRepo.findAll();
        }

        return videoList.stream().map(VideoDTO::clone).toList();
    }

}
