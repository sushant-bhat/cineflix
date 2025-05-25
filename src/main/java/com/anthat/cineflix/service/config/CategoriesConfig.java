package com.anthat.cineflix.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CategoriesConfig {
    private final ObjectMapper objectMapper;

    @Value("classpath:static/categories.json")
    private Resource configFile;
    private Map<String, Object> root;
    private Map<String, Integer> categoryMapping;


    @PostConstruct
    public void init() throws IOException {
        root = objectMapper.readValue(configFile.getInputStream(), HashMap.class);
        categoryMapping = objectMapper.convertValue(root.get("mapping"), HashMap.class);
    }

    public Map<String, Integer> getCategoryMapping() {
        return categoryMapping;
    }

    public Integer getCategoryCode(String category) {
        return categoryMapping.getOrDefault(category, 0);
    }
}
