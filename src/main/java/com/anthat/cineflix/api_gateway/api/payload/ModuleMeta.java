package com.anthat.cineflix.api_gateway.api.payload;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ModuleMeta {
    private String query;
    private String category;
}
