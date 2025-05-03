package com.anthat.cineflix.api.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Category {
    private String name;
    private Integer code;
}
