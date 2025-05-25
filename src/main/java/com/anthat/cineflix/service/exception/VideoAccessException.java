package com.anthat.cineflix.service.exception;

public class VideoAccessException extends RuntimeException {
    public VideoAccessException(String message) {
        super(message);
    }
}
