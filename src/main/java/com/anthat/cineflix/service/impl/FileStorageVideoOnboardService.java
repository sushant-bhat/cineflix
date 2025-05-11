package com.anthat.cineflix.service.impl;

import com.anthat.cineflix.dto.VideoDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.exception.VideoDeleteException;
import com.anthat.cineflix.exception.VideoUploadException;
import com.anthat.cineflix.model.Video;
import com.anthat.cineflix.repo.VideoSQLRepo;
import com.anthat.cineflix.service.TranscodeService;
import com.anthat.cineflix.service.VideoOnboardService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageVideoOnboardService implements VideoOnboardService {
    private static final Logger LOGGER = LogManager.getLogger(FileStorageVideoOnboardService.class);

    @Value("${app.upload.dir}")
    private String DIR;

    private final TranscodeService transcodeService;

    private final VideoSQLRepo videoSQLRepo;

    private String copyFileToPath(MultipartFile file, Path uploadPath) throws IOException {

        // Create the upload directory if it doesn't exist
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
    public VideoDTO uploadVideo(VideoDTO videoDetails, MultipartFile videoThumbnail, MultipartFile videoCover, MultipartFile videoFile) throws VideoUploadException {
        try {
            Video videoEntity = Video.clone(videoDetails);
            videoEntity.setVideoId(UUID.randomUUID().toString());

            String videoId = videoEntity.getVideoId();

            // Save thumbnail in a directory and update path in video thumbnail url
            Path destinationThumbnailFolderPath = Paths.get(DIR, "thumbnails", videoId);
            String thumbnailPath = copyFileToPath(videoThumbnail, destinationThumbnailFolderPath);
            videoEntity.setThumbnailContentType(videoThumbnail.getContentType());
            videoEntity.setVideoThumbnailUrl(thumbnailPath);

            // Save cover in a directory and update path in video cover url
            Path destinationCoverFolderPath = Paths.get(DIR, "covers", videoId);
            String coverPath = copyFileToPath(videoCover, destinationCoverFolderPath);
            videoEntity.setCoverContentType(videoCover.getContentType());
            videoEntity.setVideoCoverUrl(coverPath);

            // Save file in a directory and update path in video url
            Path destinationVideoFolderPath = Paths.get(DIR, "videos", videoId);
            String videoPath = copyFileToPath(videoFile, destinationVideoFolderPath);
            videoEntity.setVideoContentType(videoFile.getContentType());
            videoEntity.setVideoUrl(videoPath);

            videoEntity.setCreatedAt(System.currentTimeMillis());

            // Save video object into postgres
            videoSQLRepo.save(videoEntity);

            // Now need to transcode the video and save different versions of the file using ffmpeg
            Path destinationPath = Paths.get(DIR, "transcode", videoId);
            transcodeService.transcodeVideo(videoPath, videoId, destinationPath);

            return videoDetails;
        } catch (IOException e) {
            LOGGER.error("Error while creating folder ", e);
            throw new VideoUploadException("Something went wrong while uploading video");
        } catch (Exception e) {
            LOGGER.error("Error while uploading video", e);
            throw new VideoUploadException("Something went wrong while uploading video");
        }
    }

    // TODO: Implement to delete the thumbnail and video files as well
    @Override
    public VideoDTO removeVideo(String videoId) throws VideoAccessException, VideoDeleteException {
        try {
            Video foundVideo = videoSQLRepo.findById(videoId).orElse(null);
            if (foundVideo == null) {
                throw new VideoAccessException("Request video not found");
            }
            videoSQLRepo.deleteById(videoId);
        } catch (IllegalArgumentException exp) {
            throw new VideoAccessException(exp.getMessage());
        } catch (Exception exp) {
            throw new VideoDeleteException(exp.getMessage());
        }
        return null;
    }
}
