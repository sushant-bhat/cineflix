package com.anthat.cineflix.api_gateway.api;

import com.anthat.cineflix.api_gateway.api.payload.Category;
import com.anthat.cineflix.api_gateway.api.payload.CategoryFeedResponse;
import com.anthat.cineflix.api_gateway.api.payload.CategoryResponse;
import com.anthat.cineflix.api_gateway.api.payload.HomeFeedResponse;
import com.anthat.cineflix.api_gateway.api.payload.ModuleMeta;
import com.anthat.cineflix.api_gateway.api.payload.ModuleResponse;
import com.anthat.cineflix.api_gateway.api.payload.SearchFeedResponse;
import com.anthat.cineflix.api_gateway.api.payload.WatchListFeedResponse;
import com.anthat.cineflix.service.config.CategoriesConfig;
import com.anthat.cineflix.service.config.ModuleConfig;
import com.anthat.cineflix.service.config.ModuleType;
import com.anthat.cineflix.service.dto.UserDTO;
import com.anthat.cineflix.service.VideoMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Rest controller for API calls to get different kinds of feeds on the website
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin
public class FeedController {

    private final VideoMetaService videoMetaService;

    private final CategoriesConfig categoriesConfig;

    private ModuleResponse getModuleResponse(ModuleConfig moduleConfig) {
        return ModuleResponse.builder()
                .moduleId(moduleConfig.getModuleType().name())
                .videoList(videoMetaService.getModuleVideos(moduleConfig))
                .meta(ModuleMeta.builder()
                        .query(moduleConfig.getQuery())
                        .category(moduleConfig.getCategory()).build())
                .build();
    }

    @GetMapping("/live")
    public ResponseEntity<String> giveServerLiveStatus() {
        return ResponseEntity.ok("Running");
    }

    @GetMapping("/home")
    public ResponseEntity<HomeFeedResponse> getHomeFeed(@AuthenticationPrincipal UserDTO userDetails) {
        HomeFeedResponse homeFeedResponse = new HomeFeedResponse();

        try {
            ModuleConfig heroModuleConfig = ModuleConfig.builder().moduleType(ModuleType.HERO).build();
            homeFeedResponse.addModule(getModuleResponse(heroModuleConfig));

            ModuleConfig continueWatchModuleConfig = ModuleConfig.builder().moduleType(ModuleType.CONTINUE).username(userDetails.getUsername()).build();
            homeFeedResponse.addModule(getModuleResponse(continueWatchModuleConfig));

            ModuleConfig watchListModuleConfig = ModuleConfig.builder().moduleType(ModuleType.WATCHLIST).username(userDetails.getUsername()).build();
            homeFeedResponse.addModule(getModuleResponse(watchListModuleConfig));

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

    @GetMapping("/watchlist")
    public ResponseEntity<WatchListFeedResponse> getWatchListVideosResult(@AuthenticationPrincipal UserDTO userDetails) {
        WatchListFeedResponse watchListFeedResponse = new WatchListFeedResponse();
        try {
            ModuleConfig watchListModuleConfig = ModuleConfig.builder()
                    .moduleType(ModuleType.WATCHLIST)
                    .username(userDetails.getUsername()).build();
            watchListFeedResponse.addModule(getModuleResponse(watchListModuleConfig));
            return ResponseEntity.ok(watchListFeedResponse);
        } catch (Exception exp) {
            watchListFeedResponse.setErrorMessage("Something went wrong");
            System.out.println(exp.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(watchListFeedResponse);
        }

    }
}
