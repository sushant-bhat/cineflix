package com.anthat.cineflix.service;

import org.springframework.scheduling.annotation.Async;

import java.io.IOException;

public interface TranscodeService {
    @Async
    void transcodeVideo(String inputPath, String videoId, String destinationUrl) throws IOException, InterruptedException;
}
