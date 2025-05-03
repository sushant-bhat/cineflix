package com.anthat.cineflix.api;

import com.anthat.cineflix.api.payload.Category;
import com.anthat.cineflix.api.payload.CategoryFeedResponse;
import com.anthat.cineflix.api.payload.CategoryResponse;
import com.anthat.cineflix.api.payload.HomeFeedResponse;
import com.anthat.cineflix.api.payload.ModuleMeta;
import com.anthat.cineflix.api.payload.ModuleResponse;
import com.anthat.cineflix.api.payload.SearchFeedResponse;
import com.anthat.cineflix.config.CategoriesConfig;
import com.anthat.cineflix.config.ModuleConfig;
import com.anthat.cineflix.config.ModuleType;
import com.anthat.cineflix.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class FeedController {

    private final VideoService videoService;

    private final CategoriesConfig categoriesConfig;

    private ModuleResponse getModuleResponse(ModuleConfig moduleConfig) {
        return ModuleResponse.builder()
                .moduleId(moduleConfig.getModuleType().name())
                .videoList(videoService.getModuleVideos(moduleConfig))
                .meta(ModuleMeta.builder()
                        .query(moduleConfig.getQuery())
                        .category(moduleConfig.getCategory()).build())
                .build();
    }

    @GetMapping("/home")
    public ResponseEntity<HomeFeedResponse> getHomeFeed() {
        HomeFeedResponse homeFeedResponse = new HomeFeedResponse();

        try {
            ModuleConfig heroModuleConfig = ModuleConfig.builder().moduleType(ModuleType.HERO).build();
            homeFeedResponse.addModule(getModuleResponse(heroModuleConfig));

            ModuleConfig continueWatchModuleConfig = ModuleConfig.builder().moduleType(ModuleType.CONTINUE).build();
            homeFeedResponse.addModule(getModuleResponse(continueWatchModuleConfig));

            ModuleConfig recommendationModuleConfig = ModuleConfig.builder().moduleType(ModuleType.RECOM).build();
            homeFeedResponse.addModule(getModuleResponse(recommendationModuleConfig));

            return ResponseEntity.ok(homeFeedResponse);
        } catch (Exception exp) {
            homeFeedResponse.setErrorMessage("Something went wrong");
            System.out.println(exp.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(homeFeedResponse);
        }
    }

    @GetMapping("/category")
    public ResponseEntity<CategoryResponse> getCategories() {
        List<Category> categories = new ArrayList<>();
        categoriesConfig.getCategoryMapping().forEach((category, code) -> categories.add(new Category(category, code)));
        return ResponseEntity.ok(CategoryResponse.builder()
                .categories(categories).build());
    }

    @GetMapping("/search")
    public ResponseEntity<SearchFeedResponse> getSearchVideosResult(@RequestParam String query) {
        SearchFeedResponse searchFeedResponse = new SearchFeedResponse();
        try {
            ModuleConfig searchModuleConfig = ModuleConfig.builder()
                    .moduleType(ModuleType.SEARCH)
                    .query(query).build();
            searchFeedResponse.addModule(getModuleResponse(searchModuleConfig));
            return ResponseEntity.ok(searchFeedResponse);
        } catch (Exception exp) {
            searchFeedResponse.setErrorMessage("Something went wrong");
            System.out.println(exp.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(searchFeedResponse);
        }
    }

    @GetMapping("/browse/{cat}")
    public ResponseEntity<CategoryFeedResponse> getCategoryVideosResult(@PathVariable String cat) {
        CategoryFeedResponse categoryFeedResponse = new CategoryFeedResponse();
        try {
            ModuleConfig categoryModuleConfig = ModuleConfig.builder()
                    .moduleType(ModuleType.CAT)
                    .category(cat).build();
            categoryFeedResponse.addModule(getModuleResponse(categoryModuleConfig));
            return ResponseEntity.ok(categoryFeedResponse);
        } catch (Exception exp) {
            categoryFeedResponse.setErrorMessage("Something went wrong");
            System.out.println(exp.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(categoryFeedResponse);
        }

    }
}
