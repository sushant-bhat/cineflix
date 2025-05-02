package com.anthat.cineflix.service.impl;

import com.anthat.cineflix.config.ModuleConfig;
import com.anthat.cineflix.dto.VideoDTO;
import com.anthat.cineflix.dto.VideoStreamDTO;
import com.anthat.cineflix.dto.VideoThumbnailDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.exception.VideoDeleteException;
import com.anthat.cineflix.exception.VideoUploadException;
import com.anthat.cineflix.model.Video;
import com.anthat.cineflix.repo.VideoRepo;
import com.anthat.cineflix.service.TranscodeService;
import com.anthat.cineflix.service.VideoService;
import com.anthat.cineflix.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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
public class FileStorageVideoService implements VideoService {
    private static final Logger LOGGER = LogManager.getLogger(FileStorageVideoService.class);

    private final VideoRepo videoRepo;
    private final TranscodeService transcodeService;

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
    public VideoDTO uploadVideo(VideoDTO videoDetails, MultipartFile videoThumbnail, MultipartFile videoFile) throws VideoUploadException {
        try {
            Video videoEntity = Video.clone(videoDetails);
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
            Path destinationPath = Paths.get(DIR, "transcode", videoId);
            transcodeService.transcodeVideo(videoPath, videoId, destinationPath.toString());

            return videoDetails;
        } catch (IOException e) {
            LOGGER.error("Error while creating folder ", e);
            throw new VideoUploadException("Something went wrong while uploading video");
        } catch (Exception e) {
            LOGGER.error("Error while uploading video", e);
            throw new VideoUploadException("Something went wrong while uploading video");
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

    // TODO: Implement to update the thumbnail and video files as well
    @Override
    public VideoDTO updateVideoInfo(VideoDTO videoDetails, String videoId) {
        Video foundVideo = videoRepo.findById(videoId).orElse(null);
        if (foundVideo == null) {
            throw new VideoAccessException("Requested video not found");
        }

        foundVideo.updateFromDTO(videoDetails);
        videoRepo.save(foundVideo);
        return videoDetails;
    }

    // TODO: Implement to delete the thumbnail and video files as well
    @Override
    public VideoDTO removeVideo(String videoId) throws VideoAccessException, VideoDeleteException {
        try {
            Video foundVideo = videoRepo.findById(videoId).orElse(null);
            if (foundVideo == null) {
                throw new VideoAccessException("Request video not found");
            }
            videoRepo.deleteById(videoId);
        } catch (IllegalArgumentException exp) {
            throw new VideoAccessException(exp.getMessage());
        } catch (Exception exp) {
            throw new VideoDeleteException(exp.getMessage());
        }
        return null;
    }

    @Override
    public VideoStreamDTO fetchVideoSegment(String videoId, String fileName) {
        Path segmentPath = Paths.get(DIR, "transcode", videoId, fileName);
        if (!Files.exists(segmentPath)) {
            throw new VideoAccessException("Requested video segment not present");
        }
        return VideoStreamDTO.builder()
                .videoResource(new FileSystemResource(segmentPath))
                .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .build();
    }

    @Override
    public VideoStreamDTO fetchManifest(String videoId, String fileName) {
        Path manifestPath = Paths.get(DIR, "transcode", videoId, fileName);
        if (!Files.exists(manifestPath)) {
            throw new VideoAccessException("Requested video manifest not present");
        }
        return VideoStreamDTO.builder()
                .videoResource(new FileSystemResource(manifestPath))
                .contentType("application/vnd.apple.mpegurl")
                .build();
    }

}
