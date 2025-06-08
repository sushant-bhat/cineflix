package com.anthat.cineflix.service;

import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.nio.file.Path;

public interface TranscodeService {
    @Async
    void transcodeVideo(String videoId) throws IOException, InterruptedException;
}
