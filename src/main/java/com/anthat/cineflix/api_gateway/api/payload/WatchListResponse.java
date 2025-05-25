package com.anthat.cineflix.api_gateway.api.payload;

import com.anthat.cineflix.service.dto.WatchListDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchListResponse {
    private String errorMessage;
    private WatchListDTO watchListDetails;
}
