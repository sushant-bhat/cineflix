package com.anthat.cineflix.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleConfig {
    private ModuleType moduleType;
    private String query;
    private String category;
}
