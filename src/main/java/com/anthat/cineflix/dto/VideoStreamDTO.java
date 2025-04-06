package com.anthat.cineflix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoStreamDTO {
    private Resource videoResource;
    private String contentType;
    private HttpHeaders headers;
}
