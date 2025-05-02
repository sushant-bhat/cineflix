package com.anthat.cineflix.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class VideoTag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String catId;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;

    public VideoTag(String catId, Video video) {
        this.catId = catId;
        this.video = video;
    }
}
