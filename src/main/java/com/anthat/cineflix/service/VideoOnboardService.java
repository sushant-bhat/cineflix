package com.anthat.cineflix.service;

import com.anthat.cineflix.service.dto.VideoDTO;
import com.anthat.cineflix.service.exception.VideoAccessException;
import com.anthat.cineflix.service.exception.VideoDeleteException;
import com.anthat.cineflix.service.exception.VideoUploadException;
import org.springframework.web.multipart.MultipartFile;

public interface VideoOnboardService {
    VideoDTO uploadVideo(VideoDTO video, MultipartFile videoThumbnail, MultipartFile videoCover, MultipartFile videoFile) throws VideoUploadException;

    VideoDTO removeVideo(String videoId) throws VideoAccessException, VideoDeleteException;
}
