package com.anthat.cineflix.service;

import com.anthat.cineflix.service.config.ModuleConfig;
import com.anthat.cineflix.service.dto.VideoDTO;
import com.anthat.cineflix.service.dto.VideoProgressDTO;
import com.anthat.cineflix.service.dto.WatchListDTO;
import com.anthat.cineflix.service.exception.VideoAccessException;
import com.anthat.cineflix.service.exception.VideoUpdateException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VideoMetaService {

    VideoDTO getVideoInfoById(String userName, String videoId) throws VideoAccessException;

    VideoDTO updateVideoInfo(VideoDTO videoDetails, String videoId) throws VideoUpdateException;

    List<VideoDTO> getModuleVideos(ModuleConfig moduleConfig) throws VideoAccessException;

    WatchListDTO watchListVideo(String userName, String videoId) throws VideoAccessException;

    WatchListDTO removeWatchListVideo(String userName, String videoId);

    VideoProgressDTO updateVideoProgress(VideoProgressDTO videoProgressDetails);

    VideoProgressDTO getVideoProgress(String userName, String videoId);
}
