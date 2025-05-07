package com.anthat.cineflix.service.impl;

import com.anthat.cineflix.dto.UserDTO;
import com.anthat.cineflix.model.Authority;
import com.anthat.cineflix.model.User;
import com.anthat.cineflix.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User foundUser = userRepo.findById(username).orElseGet(null);
        if (foundUser == null) {
            throw new UsernameNotFoundException("User with username " + username + " not found");
        }

        return UserDTO.builder()
                .username(foundUser.getUserName())
                .password(foundUser.getPassword())
                .roles(foundUser.getAuthorities().stream().map(Authority::getAuthority).toList())
                .build();
    }

    public UserDTO registerUser(UserDTO userInfo, PasswordEncoder passwordEncoder) {
        try {
            User user = User.builder()
                            .userName(userInfo.getUsername())
                            .password(passwordEncoder.encode(userInfo.getPassword()))
                            .enabled(true).build();
            user.setAuthorities(userInfo.getRoles());
            userRepo.save(user);
        } catch (Exception exp) {
            userRepo.deleteById(userInfo.getUsername());
            throw new RuntimeException("User registration failed: " + exp.getMessage());
        }
        return userInfo;
    }
}
