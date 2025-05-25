package com.anthat.cineflix.api_gateway.api.payload;

import com.anthat.cineflix.service.dto.VideoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoResponse {
    private String errorMessage;
    private VideoDTO videoMeta;
}
