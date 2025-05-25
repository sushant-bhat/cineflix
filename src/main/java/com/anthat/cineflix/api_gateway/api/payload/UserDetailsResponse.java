package com.anthat.cineflix.api_gateway.api.payload;

import com.anthat.cineflix.service.dto.UserDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class UserDetailsResponse {
    private UserDTO userDetails;
    private String errorMessage;
    private String jwt;
}
