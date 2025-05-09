package com.anthat.cineflix.service.impl;

import com.anthat.cineflix.config.ModuleConfig;
import com.anthat.cineflix.dto.VideoDTO;
import com.anthat.cineflix.dto.WatchListDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.model.User;
import com.anthat.cineflix.model.Video;
import com.anthat.cineflix.repo.UserRepo;
import com.anthat.cineflix.repo.VideoRepo;
import com.anthat.cineflix.service.VideoMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostgresVideoMetaService implements VideoMetaService {
    private final VideoRepo videoRepo;

    private final UserRepo userRepo;

    @Override
    public VideoDTO getVideoInfoById(String userName, String videoId) throws VideoAccessException {
        Video foundVideoMeta = videoRepo.findById(videoId).orElse(null);
        if (foundVideoMeta == null) {
            throw new VideoAccessException("Video not found");
        }

        VideoDTO updatedVideoDetails = VideoDTO.clone(foundVideoMeta);
        Set<User> usersWatchListed = foundVideoMeta.getUsersWhoWatchListed();
        updatedVideoDetails.setWatchListedByUser(usersWatchListed.stream().anyMatch(user -> userName.equalsIgnoreCase(user.getUserName())));

        return updatedVideoDetails;
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

    @Override
    public List<VideoDTO> getModuleVideos(ModuleConfig moduleConfig) {
        List<Video> videoList = new ArrayList<>();
        switch (moduleConfig.getModuleType()) {
            case HERO -> videoList = videoRepo.findById("ee02d8cd-12d9-4c7f-8d5e-9e82b69cbd85").stream().toList();
            case CONTINUE, RECOM -> videoList = videoRepo.findAll();
            case SEARCH -> videoList = videoRepo.findAllByQuery(moduleConfig.getQuery());
            case CAT -> videoList = videoRepo.findAllByCategory(moduleConfig.getCategory());
            case WATCHLIST -> {
                return getWatchListVideos(moduleConfig.getUsername());
            }
        }

        return videoList.stream().map(VideoDTO::clone).toList();
    }

    @Override
    public WatchListDTO watchListVideo(String userName, String videoId) throws VideoAccessException {
        User foundUser = userRepo.findById(userName).orElseThrow();
        Video foundVideo = videoRepo.findById(videoId).orElseThrow();

        Set<Video> userWatchlist = foundUser.getWatchList();
        if (userWatchlist.contains(foundVideo)) {
            System.out.println("Entry already present, throw an exception in this case?");
            return null;
        }

        userWatchlist.add(foundVideo);
        userRepo.save(foundUser);
        return WatchListDTO.builder()
                .userName(foundUser.getUserName())
                .videoId(foundVideo.getVideoId()).build();
    }

    @Override
    public WatchListDTO removeWatchListVideo(String userName, String videoId) {
        User foundUser = userRepo.findById(userName).orElseThrow();
        Video foundVideo = videoRepo.findById(videoId).orElseThrow();

        Set<Video> userWatchlist = foundUser.getWatchList();
        if (!userWatchlist.contains(foundVideo)) {
            System.out.println("Entry not present, throw an exception in this case?");
            return null;
        }

        userWatchlist.remove(foundVideo);
        userRepo.save(foundUser);
        return WatchListDTO.builder()
                .userName(foundUser.getUserName())
                .videoId(foundVideo.getVideoId()).build();
    }

    @Override
    public List<VideoDTO> getWatchListVideos(String userName) {
        User foundUser = userRepo.findById(userName).orElseThrow();

        Set<Video> userWatchlist = foundUser.getWatchList();
        if (userWatchlist == null) {
            System.out.println("No watch list for the user, throw exception in this case?");
            return null;
        }

        return userWatchlist.stream().map(VideoDTO::clone).toList();
    }
}
