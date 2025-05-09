package com.anthat.cineflix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.Resource;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoImageDTO {
    private Resource imageResource;
    private String contentType;
}
