package com.anthat.cineflix.api;

import com.anthat.cineflix.api.payload.UserDetailsResponse;
import com.anthat.cineflix.dto.UserDTO;
import com.anthat.cineflix.security.JwtUtil;
import com.anthat.cineflix.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<UserDetailsResponse> register(@RequestBody UserDTO userInfo) {
        UserDTO registeredUser = userService.registerUser(userInfo, passwordEncoder);
        String jwtToken = jwtUtil.generateToken(registeredUser);
        return ResponseEntity.ok(
                UserDetailsResponse.builder()
                        .jwt(jwtToken)
                        .userDetails(registeredUser)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<UserDetailsResponse> login(@RequestBody UserDTO userInfo) {
        // TODO: Authenticate user using user name and password, generate JWT token and send in response
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userInfo.getUsername(), userInfo.getPassword()));
        UserDetails userDetails = userService.loadUserByUsername(userInfo.getUsername());
        String jwtToken = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(
                UserDetailsResponse.builder()
                        .jwt(jwtToken)
                        .userDetails((UserDTO) userDetails)
                        .build()
        );
    }

}
