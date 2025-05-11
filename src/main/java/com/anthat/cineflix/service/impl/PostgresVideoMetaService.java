package com.anthat.cineflix.service.impl;

import com.anthat.cineflix.config.ModuleConfig;
import com.anthat.cineflix.dto.VideoDTO;
import com.anthat.cineflix.dto.VideoProgressDTO;
import com.anthat.cineflix.dto.WatchListDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.model.User;
import com.anthat.cineflix.model.Video;
import com.anthat.cineflix.model.VideoProgress;
import com.anthat.cineflix.repo.UserRepo;
import com.anthat.cineflix.repo.VideoProgressSQLRepo;
import com.anthat.cineflix.repo.VideoSQLRepo;
import com.anthat.cineflix.service.VideoMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostgresVideoMetaService implements VideoMetaService {
    @Value("${app.new.arrivals.time}")
    private long newArrivalsTime;

    private final VideoSQLRepo videoSQLRepo;

    private final UserRepo userRepo;

    private final VideoProgressSQLRepo videoProgressSQLRepo;

    @Override
    public VideoDTO getVideoInfoById(String userName, String videoId) throws VideoAccessException {
        Video foundVideoMeta = videoSQLRepo.findById(videoId).orElse(null);
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
        Video foundVideo = videoSQLRepo.findById(videoId).orElse(null);
        if (foundVideo == null) {
            throw new VideoAccessException("Requested video not found");
        }

        foundVideo.updateFromDTO(videoDetails);
        videoSQLRepo.save(foundVideo);
        return videoDetails;
    }

    @Override
    public List<VideoDTO> getModuleVideos(ModuleConfig moduleConfig) {
        Collection<Video> videoList = new ArrayList<>();
        switch (moduleConfig.getModuleType()) {
            case HERO -> videoList = videoSQLRepo.findAllNewArrivals(System.currentTimeMillis() - newArrivalsTime);
            case CONTINUE -> videoList = getPendingVideos(moduleConfig.getUsername());
            case RECOM -> videoList = videoSQLRepo.findAll();
            case SEARCH -> videoList = videoSQLRepo.findAllByQuery(moduleConfig.getQuery());
            case CAT -> videoList = videoSQLRepo.findAllByCategory(moduleConfig.getCategory());
            case WATCHLIST -> videoList = getWatchListVideos(moduleConfig.getUsername());
        }

        return videoList.stream().map(VideoDTO::clone).toList();
    }

    private List<Video> getPendingVideos(String userName) {
        List<VideoProgress> videoProgressList = videoProgressSQLRepo.findAllByUserName(userName);
        if (CollectionUtils.isEmpty(videoProgressList)) {
            return Collections.emptyList();
        }
        return videoProgressList.stream().filter(vp -> vp.getLastWatched() < vp.getDuration()).map(VideoProgress::getVideo).toList();
    }

    @Override
    public WatchListDTO watchListVideo(String userName, String videoId) throws VideoAccessException {
        User foundUser = userRepo.findById(userName).orElseThrow();
        Video foundVideo = videoSQLRepo.findById(videoId).orElseThrow();

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
        Video foundVideo = videoSQLRepo.findById(videoId).orElseThrow();

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

    private Set<Video> getWatchListVideos(String userName) {
        User foundUser = userRepo.findById(userName).orElseThrow();

        Set<Video> userWatchlist = foundUser.getWatchList();
        if (userWatchlist == null) {
            System.out.println("No watch list for the user, throw exception in this case?");
            return Collections.emptySet();
        }

        return userWatchlist;
    }

    @Override
    public VideoProgressDTO updateVideoProgress(VideoProgressDTO videoProgressDetails) {
        VideoProgress foundVideoProgress = videoProgressSQLRepo.findByUserNameAndVideoId(videoProgressDetails.getUserName(), videoProgressDetails.getVideoId())
                .orElseGet(() -> {
                    User foundUser = userRepo.findById(videoProgressDetails.getUserName()).orElseThrow();
                    Video foundVideo = videoSQLRepo.findById(videoProgressDetails.getVideoId()).orElseThrow();
                    return VideoProgress.builder().user(foundUser).video(foundVideo).build();
                });

        foundVideoProgress.setLastWatched(videoProgressDetails.getLastWatched());
        foundVideoProgress.setDuration(videoProgressDetails.getDuration());

        videoProgressSQLRepo.save(foundVideoProgress);

        return videoProgressDetails;
    }

    @Override
    public VideoProgressDTO getVideoProgress(String userName, String videoId) {
        VideoProgress videoProgress = videoProgressSQLRepo.findByUserNameAndVideoId(userName, videoId)
                .orElseGet(() -> VideoProgress.builder()
                        .lastWatched(0L)
                        .duration(1L).build());

        return VideoProgressDTO.builder()
                .userName(userName)
                .videoId(videoId)
                .lastWatched(videoProgress.getLastWatched())
                .duration(videoProgress.getDuration()).build();
    }
}
