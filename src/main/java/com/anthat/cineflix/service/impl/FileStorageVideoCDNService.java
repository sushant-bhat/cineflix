package com.anthat.cineflix.service.impl;

import com.anthat.cineflix.dto.VideoStreamDTO;
import com.anthat.cineflix.dto.VideoImageDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.model.Video;
import com.anthat.cineflix.repo.VideoRepo;
import com.anthat.cineflix.service.VideoCDNService;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class FileStorageVideoCDNService implements VideoCDNService {
    private static final Logger LOGGER = LogManager.getLogger(FileStorageVideoCDNService.class);

    private final VideoRepo videoRepo;

    @Value("${app.upload.dir}")
    private String DIR;

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
    public VideoImageDTO getVideoThumbnailById(String videoId) throws VideoAccessException {
        Video foundVideoMeta = videoRepo.findById(videoId).orElse(null);
        if (foundVideoMeta == null) {
            throw new VideoAccessException("Video record not found");
        }

        Path filePath = Paths.get(foundVideoMeta.getVideoThumbnailUrl());
        if (!Files.exists(filePath)) {
            throw new VideoAccessException("Video thumbnail not found");
        }

        return VideoImageDTO.builder()
                .imageResource(new FileSystemResource(filePath))
                .contentType(foundVideoMeta.getThumbnailContentType())
                .build();
    }

    @Override
    public VideoImageDTO getVideoCoverById(String videoId) throws VideoAccessException {
        Video foundVideoMeta = videoRepo.findById(videoId).orElse(null);
        if (foundVideoMeta == null) {
            throw new VideoAccessException("Video record not found");
        }

        Path filePath = Paths.get(foundVideoMeta.getVideoCoverUrl());
        if (!Files.exists(filePath)) {
            throw new VideoAccessException("Video thumbnail not found");
        }

        return VideoImageDTO.builder()
                .imageResource(new FileSystemResource(filePath))
                .contentType(foundVideoMeta.getCoverContentType())
                .build();
    }

    @Override
    public VideoStreamDTO fetchVideoSegment(String videoId, String fileName) {
        Video foundVideo = videoRepo.findById(videoId).orElseThrow();
        Path segmentPath = Paths.get(foundVideo.getTranscodedVideoSegmentUrl(), fileName);
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
        Video foundVideo = videoRepo.findById(videoId).orElseThrow();
        Path manifestPath = Paths.get(foundVideo.getTranscodedVideoManifestUrl(), fileName);
        if (!Files.exists(manifestPath)) {
            throw new VideoAccessException("Requested video manifest not present");
        }
        return VideoStreamDTO.builder()
                .videoResource(new FileSystemResource(manifestPath))
                .contentType("application/vnd.apple.mpegurl")
                .build();
    }

}
