package com.anthat.cineflix.service;

import com.anthat.cineflix.dto.VideoStreamDTO;
import com.anthat.cineflix.dto.VideoImageDTO;
import com.anthat.cineflix.exception.VideoAccessException;
import org.springframework.stereotype.Service;

@Service
public interface VideoCDNService {

    VideoStreamDTO getVideoStreamById(String videoId, String range) throws VideoAccessException;

    VideoImageDTO getVideoThumbnailById(String videoId) throws VideoAccessException;

    VideoImageDTO getVideoCoverById(String videoId) throws VideoAccessException;

    VideoStreamDTO fetchVideoSegment(String videoId, String fileName);

    VideoStreamDTO fetchManifest(String videoId, String fileName);

}
