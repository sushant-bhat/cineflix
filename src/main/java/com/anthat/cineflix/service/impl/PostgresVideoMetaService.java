package com.anthat.cineflix.service.impl;

import com.anthat.cineflix.config.ModuleConfig;
import com.anthat.cineflix.dto.VideoDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.model.Video;
import com.anthat.cineflix.repo.VideoRepo;
import com.anthat.cineflix.service.VideoMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostgresVideoMetaService implements VideoMetaService {
    private final VideoRepo videoRepo;

    @Override
    public VideoDTO getVideoInfoById(String videoId) throws VideoAccessException {
        Video foundVideoMeta = videoRepo.findById(videoId).orElse(null);
        if (foundVideoMeta == null) {
            throw new VideoAccessException("Video not found");
        }

        return VideoDTO.clone(foundVideoMeta);
    }

    @Override
    public List<VideoDTO> getModuleVideos(ModuleConfig moduleConfig) {
        List<Video> videoList = new ArrayList<>();
        switch (moduleConfig.getModuleType()) {
            case HERO -> videoList = videoRepo.findById("ee02d8cd-12d9-4c7f-8d5e-9e82b69cbd85").stream().toList();
            case CONTINUE, RECOM -> videoList = videoRepo.findAll();
            case SEARCH -> videoList = videoRepo.findAllByQuery(moduleConfig.getQuery());
            case CAT -> videoList = videoRepo.findAllByCategory(moduleConfig.getCategory());
        }

        return videoList.stream().map(VideoDTO::clone).toList();
    }

    // TODO: Implement to update the thumbnail and video files as well
    @Override
    public VideoDTO updateVideoInfo(VideoDTO videoDetails, String videoId) {
        Video foundVideo = videoRepo.findById(videoId).orElse(null);
        if (foundVideo == null) {
            throw new VideoAccessException("Requested video not found");
        }

        foundVideo.updateFromDTO(videoDetails);
        videoRepo.save(foundVideo);
        return videoDetails;
    }
}
