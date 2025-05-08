package com.anthat.cineflix.service;

import com.anthat.cineflix.config.ModuleConfig;
import com.anthat.cineflix.dto.VideoDTO;
import com.anthat.cineflix.dto.WatchListDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.exception.VideoUpdateException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VideoMetaService {

    VideoDTO getVideoInfoById(String videoId) throws VideoAccessException;

    VideoDTO updateVideoInfo(VideoDTO videoDetails, String videoId) throws VideoUpdateException;

    List<VideoDTO> getModuleVideos(ModuleConfig moduleConfig) throws VideoAccessException;

    WatchListDTO watchListVideo(String userName, String videoId) throws VideoAccessException;

    WatchListDTO removeWatchListVideo(String userName, String videoId);

    List<VideoDTO> getWatchListVideos(String userName);
}
