package com.anthat.cineflix.api;

import com.anthat.cineflix.api.payload.CategoryFeedResponse;
import com.anthat.cineflix.api.payload.HomeFeedResponse;
import com.anthat.cineflix.api.payload.ModuleResponse;
import com.anthat.cineflix.api.payload.SearchFeedResponse;
import com.anthat.cineflix.config.ModuleConfig;
import com.anthat.cineflix.config.ModuleType;
import com.anthat.cineflix.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final VideoService videoService;

    private ModuleResponse getModuleResponse(ModuleConfig moduleConfig) {
        return ModuleResponse.builder()
                        .moduleId(moduleConfig.getModuleType().name())
                        .videoList(videoService.getModuleVideos(moduleConfig))
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

    @GetMapping("/search")
    public ResponseEntity<SearchFeedResponse> getSearchVideosResult(@RequestParam String query) {
        return ResponseEntity.ok(new SearchFeedResponse());
    }

    @GetMapping("/browse/{catId}")
    public ResponseEntity<CategoryFeedResponse> getCategoryVideosResult(@PathVariable int catId) {
        return ResponseEntity.ok(new CategoryFeedResponse());
    }
}
