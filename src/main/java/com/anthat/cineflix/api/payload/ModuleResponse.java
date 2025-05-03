package com.anthat.cineflix.api.payload;

import com.anthat.cineflix.dto.VideoDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ModuleResponse {
    private String errorMessage;
    private String moduleId;
    private ModuleMeta meta;
    private List<VideoDTO> videoList;
}
