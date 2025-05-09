package com.anthat.cineflix.api;

import com.anthat.cineflix.api.payload.VideoProgressResponse;
import com.anthat.cineflix.api.payload.VideoResponse;
import com.anthat.cineflix.dto.UserDTO;
import com.anthat.cineflix.dto.VideoDTO;
import com.anthat.cineflix.dto.VideoProgressDTO;
import com.anthat.cineflix.dto.VideoThumbnailDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.exception.VideoDeleteException;
import com.anthat.cineflix.exception.VideoUpdateException;
import com.anthat.cineflix.service.VideoMetaService;
import com.anthat.cineflix.service.VideoOnboardService;
import com.anthat.cineflix.service.VideoCDNService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Rest controller for API calls corresponding to actions performed by the user/system affecting video resource
 */
@RestController
@RequestMapping("/api/v1/video")
@CrossOrigin
@RequiredArgsConstructor
public class VideoController {

    private final VideoCDNService videoCDNService;

    private final VideoOnboardService videoOnboardService;

    private final VideoMetaService videoMetaService;

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoResponse> getVideoDetails(@AuthenticationPrincipal UserDTO userDetails, @PathVariable String videoId) {
        try {
            VideoDTO videoMeta = videoMetaService.getVideoInfoById(userDetails.getUsername(), videoId);
            return ResponseEntity.ok(VideoResponse.builder().videoMeta(videoMeta).build());
        } catch (VideoAccessException exp) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(VideoResponse.builder().errorMessage(exp.getMessage()).build());
        } catch (Exception exp) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VideoResponse.builder().errorMessage(exp.getMessage()).build());
        }
    }

    @GetMapping("/{videoId}/thumb")
    public ResponseEntity<Resource> getVideoThumbnail(@PathVariable String videoId) {
        try {
            VideoThumbnailDTO thumbnail = videoCDNService.getVideoThumbnailById(videoId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(thumbnail.getContentType()))
                    .body(thumbnail.getThumbnailResource());
        } catch (VideoAccessException exp) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception exp) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PreAuthorize("hasRole('CREATOR')")
    @GetMapping("/{videoId}/edit")
    public ResponseEntity<VideoResponse> getVideoDetailsForEdit(@AuthenticationPrincipal UserDTO userDetails, @PathVariable String videoId) {
        try {
            VideoDTO videoMeta = videoMetaService.getVideoInfoById(userDetails.getUsername(), videoId);
            return ResponseEntity.ok(VideoResponse.builder().videoMeta(videoMeta).build());
        } catch (VideoAccessException exp) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(VideoResponse.builder().errorMessage(exp.getMessage()).build());
        } catch (Exception exp) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VideoResponse.builder().errorMessage(exp.getMessage()).build());
        }
    }

    @PreAuthorize("hasRole('CREATOR')")
    @PostMapping
    public ResponseEntity<VideoResponse> uploadVideo(@RequestPart VideoDTO video, @RequestPart("thumbnail") MultipartFile videoThumbnail, @RequestPart("file") MultipartFile videoFile) {
        try {
            VideoDTO videoDetails = videoOnboardService.uploadVideo(video, videoThumbnail, videoFile);
            return ResponseEntity.ok(VideoResponse.builder().videoMeta(videoDetails).build());
        } catch (Exception exp) {
            // TODO: Add log
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VideoResponse.builder().errorMessage("Couldn't upload video").build());
        }
    }

    @PreAuthorize("hasRole('CREATOR')")
    @PutMapping("/{videoId}")
    public ResponseEntity<VideoResponse> updateVideo(@RequestBody VideoDTO videoDetails, @PathVariable String videoId) {
        try {
            VideoDTO updatedVideoDetails = videoMetaService.updateVideoInfo(videoDetails, videoId);
            return ResponseEntity.ok(VideoResponse.builder().videoMeta(updatedVideoDetails).build());
        } catch (VideoUpdateException exp) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VideoResponse.builder().errorMessage("Something went wrong when updating").build());
        } catch (VideoAccessException exp) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(VideoResponse.builder().errorMessage("Couldn't access the video requested").build());
        }
    }

    @PreAuthorize("hasRole('CREATOR')")
    @DeleteMapping("/{videoId}")
    public ResponseEntity<VideoResponse> deleteVideo(@PathVariable String videoId) {
        try {
            VideoDTO deletedVideoDetails = videoOnboardService.removeVideo(videoId);
            return ResponseEntity.ok(VideoResponse.builder().videoMeta(deletedVideoDetails).build());
        } catch (VideoAccessException exp) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(VideoResponse.builder().errorMessage("Couldn't access the video requested").build());
        } catch (VideoDeleteException exp) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VideoResponse.builder().errorMessage("Something went wrong when deleting the video").build());
        }
    }

    @PostMapping("/{videoId}/progress")
    public ResponseEntity<VideoProgressResponse> updateVideoProgressForUser(@AuthenticationPrincipal UserDTO userDetails, @PathVariable String videoId, @RequestBody VideoProgressDTO videoProgressDetails) {
        videoProgressDetails.setUserName(userDetails.getUsername());
        videoProgressDetails.setVideoId(videoId);

        VideoProgressDTO updatedVideoProgressDetails = videoMetaService.updateVideoProgress(videoProgressDetails);

        return ResponseEntity.ok(VideoProgressResponse.builder()
                .videoProgressDetails(updatedVideoProgressDetails).build());
    }

    @GetMapping("/{videoId}/progress")
    public ResponseEntity<VideoProgressResponse> getVideoProgressForUser(@AuthenticationPrincipal UserDTO userDetails, @PathVariable String videoId) {
        VideoProgressDTO videoProgressDetails = videoMetaService.getVideoProgress(userDetails.getUsername(), videoId);

        return ResponseEntity.ok(VideoProgressResponse.builder()
                .videoProgressDetails(videoProgressDetails).build());
    }
}
