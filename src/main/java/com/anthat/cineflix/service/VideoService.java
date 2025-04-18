package com.anthat.cineflix.service;

import com.anthat.cineflix.config.ModuleConfig;
import com.anthat.cineflix.dto.VideoDTO;
import com.anthat.cineflix.dto.VideoStreamDTO;
import com.anthat.cineflix.dto.VideoThumbnailDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.exception.VideoDeleteException;
import com.anthat.cineflix.exception.VideoUpdateException;
import com.anthat.cineflix.exception.VideoUploadException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {
    VideoDTO uploadVideo(VideoDTO video, MultipartFile videoThumbnail, MultipartFile videoFile) throws VideoUploadException;

    VideoDTO getVideoInfoById(String videoId) throws VideoAccessException;

    VideoStreamDTO getVideoStreamById(String videoId, String range) throws VideoAccessException;

    VideoThumbnailDTO getVideoThumbnailById(String videoId) throws VideoAccessException;

    List<VideoDTO> getModuleVideos(ModuleConfig heroModuleConfig) throws VideoAccessException;

    VideoDTO updateVideoInfo(VideoDTO videoDetails, String videoId) throws VideoUpdateException;

    VideoDTO removeVideo(String videoId) throws VideoAccessException, VideoDeleteException;

    VideoStreamDTO fetchVideoSegment(String videoId, String fileName);

    VideoStreamDTO fetchManifest(String videoId, String fileName);
}
