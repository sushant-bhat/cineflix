package com.anthat.cineflix.api.payload;

import com.anthat.cineflix.dto.WatchListDTO;
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
