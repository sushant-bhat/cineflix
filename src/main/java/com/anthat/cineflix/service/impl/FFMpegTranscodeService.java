package com.anthat.cineflix.service.impl;

import com.anthat.cineflix.service.TranscodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FFMpegTranscodeService implements TranscodeService {

    @Value("${app.upload.dir}")
    private String DIR;

    @Override
    public void transcodeVideo(String originalFileName, String inputPath, String videoId) throws IOException, InterruptedException {
        String[] resolutions = {"640x360", "854x480", "1280x720", "1920x1080"};
        String[] bitRates = {"500k", "800k", "1500k", "4000k"};

        Path transcodePath = Paths.get(DIR, "transcode", videoId);
        if (!Files.exists(transcodePath)) {
            Files.createDirectories(transcodePath);
        }

        // Get the file name and create the destination file path
        String fileName = StringUtils.stripFilenameExtension(originalFileName);
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
}
