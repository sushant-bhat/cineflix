package com.anthat.cineflix.api.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WatchListFeedResponse {
    private String errorMessage;
    private List<ModuleResponse> modules;

    public void addModule(ModuleResponse module) {
        if (modules == null) {
            modules = new ArrayList<>();
        }
        modules.add(module);
    }
}
