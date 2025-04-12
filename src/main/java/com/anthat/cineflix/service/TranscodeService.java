package com.anthat.cineflix.service;

import java.io.IOException;

public interface TranscodeService {
    void transcodeVideo(String inputPath, String videoId, String destinationUrl) throws IOException, InterruptedException;
}
