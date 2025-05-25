package com.anthat.cineflix.api_gateway.api.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CategoryResponse {
    private String errorMessage;
    private List<Category> categories;
}
