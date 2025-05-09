package com.anthat.cineflix.dto;

import com.anthat.cineflix.model.Video;
import com.anthat.cineflix.model.VideoCast;
import com.anthat.cineflix.model.VideoTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoDTO {
    private String videoId;

    private String videoTitle;

    private String videoDescription;

    private List<String> videoTags;

    private List<String> videoCast;

    private String videoDirector;

    private boolean isWatchListedByUser;

    public static VideoDTO clone(Video videoMeta) {
        VideoDTO videoDTO = VideoDTO.builder()
                .videoId(videoMeta.getVideoId())
                .videoTitle(videoMeta.getVideoTitle())
                .videoDescription(videoMeta.getVideoDescription())
                .videoDirector(videoMeta.getVideoDirector()).build();

        videoDTO.setVideoTagsFromMeta(videoMeta.getVideoTags());
        videoDTO.setVideoCastFromMeta(videoMeta.getVideoCast());

        return videoDTO;
    }

    private void setVideoTagsFromMeta(List<VideoTag> tags) {
        videoTags = tags.stream().map(VideoTag::getCatId).toList();
    }

    private void setVideoCastFromMeta(List<VideoCast> casts) {
        videoCast = casts.stream().map(VideoCast::getName).toList();
    }
}
