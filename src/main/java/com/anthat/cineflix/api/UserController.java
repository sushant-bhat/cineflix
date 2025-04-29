package com.anthat.cineflix.api;

import com.anthat.cineflix.api.payload.UserDetailsResponse;
import com.anthat.cineflix.dto.UserDTO;
import com.anthat.cineflix.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDetailsResponse> register(@RequestBody UserDTO userInfo) {
        UserDTO registeredUser = userService.registerUser(userInfo);
        return ResponseEntity.ok(
                UserDetailsResponse.builder()
                        .userDetails(registeredUser)
                        .errorMessage(null)
                        .build()
        );
    }

}
