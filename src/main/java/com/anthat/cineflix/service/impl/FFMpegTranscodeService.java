package com.anthat.cineflix.service.impl;

import com.anthat.cineflix.data.model.Video;
import com.anthat.cineflix.data.repo.VideoSQLRepo;
import com.anthat.cineflix.service.TranscodeService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * This service is responsible for transcoding original video into different resolutions and segmenting, and stores them in the specified destination
 */
@Service
@Getter
@Setter
@RequiredArgsConstructor
public class FFMpegTranscodeService implements TranscodeService {

    private final VideoSQLRepo videoSQLRepo;

    private final Executor transcodeTaskExecutor;

    private String generateHLSFilePrefix(String base, String resolution) {
        return base + resolution.split("x")[1] + "p";
    }

    @Override
    @Async
    public void transcodeVideo(String sourceVideoUrl, String videoId, Path destinationPath) throws IOException {
        List<String> resolutions = List.of("640x360", "854x480", "1280x720", "1920x1080");
        List<String> bitRates = List.of("500k", "800k", "1500k", "4000k");

        // /Users/s1b030z/Repo/cineflix/data/transcode/<id>
        if (!Files.exists(destinationPath)) {
            Files.createDirectories(destinationPath);
        }

        CountDownLatch executorCountDownLatch = new CountDownLatch(resolutions.size());

        for (int i = 0; i < resolutions.size(); i++) {
            final String resolution = resolutions.get(i);
            final String bitrate = bitRates.get(i);
            CompletableFuture.runAsync(() ->
                    {
                        try {
                            executeResolutionTranscode(resolution, bitrate, sourceVideoUrl, destinationPath);
                        } catch (IOException | InterruptedException exp) {
                            System.out.println("Exception while executing transcoding " + exp.getMessage());
                        }
                    },
                    transcodeTaskExecutor)
                    .exceptionally(exp -> {
                        System.out.println("Exception while executing transcoding " + exp.getMessage());
                        return null;
                    })
                    .thenRun(executorCountDownLatch::countDown);
        }

        try {
            executorCountDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        createMasterPlaylist(videoId, destinationPath, resolutions);
    }

    private void executeResolutionTranscode(String resolution, String bitrate, String sourceVideoUrl, Path destinationPath) throws IOException, InterruptedException {

        String segmentFilePrefix = destinationPath.resolve(generateHLSFilePrefix("segment_", resolution)).toString();
        String indexFilePrefix = destinationPath.resolve(generateHLSFilePrefix("index_", resolution)).toString();

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(sourceVideoUrl);
        command.add("-vf");
        command.add(String.format("scale=%s", resolution));
        command.add("-c:v");
        command.add("libx264");
        command.add("-b:v");
        command.add(bitrate);
        command.add("-preset");
        command.add("veryfast");
        command.add("-hls_time");
        command.add("10");
        command.add("-hls_list_size");
        command.add("0");
        command.add("-hls_segment_filename");
        command.add(segmentFilePrefix + "_%05d.ts"); // /Users/s1b030z/Repo/cineflix/data/transcode/<id>/segment_360p_1.ts
        command.add(indexFilePrefix + ".m3u8"); // /Users/s1b030z/Repo/cineflix/data/transcode/<id>/360p/index_360p.m3u8

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.redirectErrorStream(true).start();

        boolean finished = process.waitFor(60, TimeUnit.SECONDS);

        if (!finished || process.exitValue() != 0) {
            System.err.println("FFmpeg processing failed for resolution: " + resolution);
            InputStream errorStream = process.getInputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = errorStream.read(buffer)) != -1) {
                System.err.print(new String(buffer, 0, len));
            }
        } else {
            System.out.println("FFmpeg processing successful for resolution: " + resolution);
        }
    }

    private void createMasterPlaylist(String videoId, Path outputDir, List<String> resolutions) throws IOException {
        Path masterPlaylistFilePath = outputDir.resolve( "master.m3u8");
        StringBuilder masterPlaylistContent = new StringBuilder("#EXTM3U\n");

        for (String resolution : resolutions) {
            String bandwidth = getApproximateBandwidth(resolution); // Implement this logic
            masterPlaylistContent
                    .append(String.format("#EXT-X-MEDIA:TYPE=VIDEO,GROUP-ID=\"media\",NAME=\"%s\",DEFAULT=NO,AUTOSELECT=YES,URI=\"index_%sp.m38u\"\n", resolution, resolution.split("x")[1]));
            masterPlaylistContent
                    .append(String.format("#EXT-X-STREAM-INF:BANDWIDTH=%s,RESOLUTION=%s,CODECS=\"avc1.42c015, mp4a.40.2\",GROUP-ID=\"media\"\n", bandwidth, resolution))
                    .append(String.format("index_%sp.m3u8\n", resolution.split("x")[1]));
        }

        java.nio.file.Files.writeString(masterPlaylistFilePath, masterPlaylistContent.toString());

        Video video = videoSQLRepo.findById(videoId).orElseThrow();
        video.setTranscodedVideoManifestUrl(outputDir.toString());
        video.setTranscodedVideoSegmentUrl(outputDir.toString());
        videoSQLRepo.save(video);
    }

    private String getApproximateBandwidth(String resolution) {
        // Implement logic to estimate bandwidth based on resolution
        // This is a simplified example
        return switch (resolution) {
            case "640x360" -> "1000000";
            case "854x480" -> "2000000";
            case "1280x720" -> "3000000";
            default -> "6000000";
        };
    }
}
