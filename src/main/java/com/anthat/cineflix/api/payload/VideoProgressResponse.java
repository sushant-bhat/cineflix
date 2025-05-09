package com.anthat.cineflix.api.payload;

import com.anthat.cineflix.dto.VideoProgressDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class VideoProgressResponse {
    private String errorMessage;
    private VideoProgressDTO videoProgressDetails;
}
