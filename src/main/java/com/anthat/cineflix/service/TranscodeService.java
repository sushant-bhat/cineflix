package com.anthat.cineflix.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface TranscodeService {
    void transcodeVideo(String fileName, String inputPath, String videoId) throws IOException, InterruptedException;
}
