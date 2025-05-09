package com.anthat.cineflix.model;

import com.anthat.cineflix.dto.VideoDTO;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "videos")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Video {
    @Id
    private String videoId;

    private String videoTitle;

    private String videoDescription;

    @ManyToMany(mappedBy = "watchList")
    private Set<User> usersWhoWatchListed = new HashSet<>();

    @OneToMany(mappedBy = "video", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VideoTag> videoTags;

    @OneToMany(mappedBy = "video", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<VideoCast> videoCast;

    private String videoDirector;

    private String thumbnailContentType;

    private String videoThumbnailUrl;

    private String videoContentType;

    private String videoUrl;

    private String transcodedVideoSegmentUrl;

    private String transcodedVideoManifestUrl;

    public static Video clone(VideoDTO videoDTO) {
        Video video = Video.builder()
                .videoTitle(videoDTO.getVideoTitle())
                .videoDescription(videoDTO.getVideoDescription())
                .videoDirector(videoDTO.getVideoDirector())
                .build();

        video.setVideoTagsFromDTO(videoDTO.getVideoTags());
        video.setVideoCastFromDTO(videoDTO.getVideoCast());

        return video;
    }

    public void updateFromDTO(VideoDTO videoDTO) {
        setVideoTitle(videoDTO.getVideoTitle());
        setVideoDescription(videoDTO.getVideoDescription());
        setVideoDirector(videoDTO.getVideoDirector());
        setVideoTagsFromDTO(videoDTO.getVideoTags());
        setVideoCastFromDTO(videoDTO.getVideoCast());
    }

    private void setVideoTagsFromDTO(List<String> tags) {
        videoTags = tags.stream().map(tag -> new VideoTag(tag, this)).toList();
    }

    private void setVideoCastFromDTO(List<String> cast) {
        videoCast = cast.stream().map(name -> new VideoCast(name, this)).toList();
    }
}
