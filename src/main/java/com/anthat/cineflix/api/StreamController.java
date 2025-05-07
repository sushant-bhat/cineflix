package com.anthat.cineflix.api;

import com.anthat.cineflix.dto.VideoStreamDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import com.anthat.cineflix.service.VideoCDNService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/video")
@CrossOrigin
@RequiredArgsConstructor
public class StreamController {

    private final VideoCDNService videoCDNService;

    @GetMapping("/{videoId}/stream")
    public ResponseEntity<Resource> getVideoForStreaming(@PathVariable String videoId, @RequestHeader(value = "Range", required = false) String range) {
        try {
            VideoStreamDTO videoStream = videoCDNService.getVideoStreamById(videoId, range);
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

    @GetMapping(value = "/{videoId}/{filename:.+}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getVideoSegment(@PathVariable String videoId, @PathVariable String filename) {
        try {
            VideoStreamDTO videoStreamDTO = videoCDNService.fetchVideoSegment(videoId, filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(videoStreamDTO.getContentType()))
                    .body(videoStreamDTO.getVideoResource());
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

    @GetMapping(value = "/{videoId}/{filename:.+}", produces = "application/vnd.apple.mpegurl")
    public ResponseEntity<Resource> getVideoManifest(@PathVariable String videoId, @PathVariable String filename) {
        try {
            VideoStreamDTO videoStreamDTO = videoCDNService.fetchManifest(videoId, filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(videoStreamDTO.getContentType()))
                    .body(videoStreamDTO.getVideoResource());
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
}
