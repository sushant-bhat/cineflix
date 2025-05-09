package com.anthat.cineflix.service;

import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.nio.file.Path;

public interface TranscodeService {
    @Async
    void transcodeVideo(String inputPath, String videoId, Path destinationPath) throws IOException, InterruptedException;
}
