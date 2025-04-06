package com.anthat.cineflix.api.payload;

import com.anthat.cineflix.dto.VideoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.Resource;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoResponse {
    private String errorMessage;
    private VideoDTO videoMeta;
}
