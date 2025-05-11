package com.anthat.cineflix.service.impl;

import com.anthat.cineflix.model.Video;
import com.anthat.cineflix.repo.VideoSQLRepo;
import com.anthat.cineflix.service.TranscodeService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This service is responsible for transcoding original video into different resolutions and segmenting, and stores them in the specified destination
 */
@Service
public class FFMpegTranscodeService implements TranscodeService {

    private final VideoSQLRepo videoSQLRepo;

    public FFMpegTranscodeService(VideoSQLRepo videoSQLRepo) {
        this.videoSQLRepo = videoSQLRepo;
    }

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

        for (int i = 0; i < resolutions.size(); i++) {
            String resolution = resolutions.get(i);
            String bitrate = bitRates.get(i);

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
            try {
                boolean finished = process.waitFor(60, TimeUnit.SECONDS);

                if (!finished || process.exitValue() != 0) {
                    System.err.println("FFmpeg processing failed for resolution: " + resolution);
                    java.io.InputStream errorStream = process.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = errorStream.read(buffer)) != -1) {
                        System.err.print(new String(buffer, 0, len));
                    }
                } else {
                    System.out.println("FFmpeg processing successful for resolution: " + resolution);
                }
            } catch (InterruptedException exp) {
                System.out.println(exp.getMessage());
            }
        }

        createMasterPlaylist(videoId, destinationPath, resolutions);
    }

    private void createMasterPlaylist(String videoId, Path outputDir, List<String> resolutions) throws IOException {
        Path masterPlaylistFilePath = outputDir.resolve( "master.m3u8");
        StringBuilder masterPlaylistContent = new StringBuilder("#EXTM3U\n");

        for (String resolution : resolutions) {
            String bandwidth = getApproximateBandwidth(resolution); // Implement this logic
            masterPlaylistContent
                    .append("#EXT-X-MEDIA:TYPE=VIDEO,GROUP-ID=\"media\",NAME=\"")
                    .append(resolution)
                    .append("\",DEFAULT=NO,AUTOSELECT=YES,URI=\"")
                    .append("index_").append(resolution.split("x")[1]).append("p.m3u8\n");
            masterPlaylistContent.append("#EXT-X-STREAM-INF:BANDWIDTH=").append(bandwidth).append(",RESOLUTION=")
                    .append(resolution).append(",CODECS=\"avc1.42c015, mp4a.40.2\",GROUP-ID=\"media\"\n")
                    .append("index_").append(resolution.split("x")[1]).append("p.m3u8\n");
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
        if (resolution.equals("640x360")) return "1000000";
        if (resolution.equals("1280x720")) return "3000000";
        return "2000000";
    }
}
