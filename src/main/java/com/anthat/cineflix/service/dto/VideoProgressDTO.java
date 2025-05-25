package com.anthat.cineflix.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class VideoProgressDTO {
    private String userName;
    private String videoId;
    private Long lastWatched;
    private Long duration;
}
