package com.anthat.cineflix.service;

import com.anthat.cineflix.dto.VideoDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.exception.VideoDeleteException;
import com.anthat.cineflix.exception.VideoUploadException;
import org.springframework.web.multipart.MultipartFile;

public interface VideoOnboardService {
    VideoDTO uploadVideo(VideoDTO video, MultipartFile videoThumbnail, MultipartFile videoCover, MultipartFile videoFile) throws VideoUploadException;

    VideoDTO removeVideo(String videoId) throws VideoAccessException, VideoDeleteException;
}
