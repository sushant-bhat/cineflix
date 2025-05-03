package com.anthat.cineflix.api.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class CategoryResponse {
    private String errorMessage;
    private List<Category> categories;
}
