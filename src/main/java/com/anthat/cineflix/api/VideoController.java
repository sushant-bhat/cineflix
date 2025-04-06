package com.anthat.cineflix.api;

import com.anthat.cineflix.api.payload.VideoResponse;
import com.anthat.cineflix.dto.VideoDTO;
import com.anthat.cineflix.dto.VideoStreamDTO;
import com.anthat.cineflix.dto.VideoThumbnailDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.exception.VideoUploadException;
import com.anthat.cineflix.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/video")
@CrossOrigin
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoResponse> getVideoDetails(@PathVariable String videoId) {
        try {
            VideoDTO videoMeta = videoService.getVideoInfoById(videoId);
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
            VideoThumbnailDTO thumbnail = videoService.getVideoThumbnailById(videoId);
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

    @GetMapping("/{videoId}/stream")
    public ResponseEntity<Resource> getVideoForStreaming(@PathVariable String videoId, @RequestHeader(value = "Range", required = false) String range) {
        try {
            VideoStreamDTO videoStream = videoService.getVideoStreamById(videoId, range);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(videoStream.getHeaders())
                    .contentType(MediaType.parseMediaType(videoStream.getContentType()))
                    .body(videoStream.getVideoResource());
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

    @GetMapping("/{videoId}/edit")
    public ResponseEntity<VideoResponse> getVideoDetailsForEdit(@PathVariable String videoId) {
        try {
            VideoDTO videoMeta = videoService.getVideoInfoById(videoId);
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

    @GetMapping("/browse/{catId}")
    public String getCategoryVideosResult(@PathVariable int catId) {
        return "Showing videos of category " + catId;
    }

    @PostMapping
    public ResponseEntity<String> uploadVideo(@RequestPart VideoDTO video, @RequestPart("thumbnail") MultipartFile videoThumbnail, @RequestPart("file") MultipartFile videoFile) {
        try {
            videoService.uploadVideo(video, videoThumbnail, videoFile);
        } catch (VideoUploadException exp) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exp.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Video uploaded");
    }

    @PutMapping("/{videoId}")
    public void updateVideo(@PathVariable int videoId) {

    }

    @GetMapping("/search")
    public String getSearchVideosResult(@RequestParam String query) {
        return "Showing videos for query " + query;
    }
}
