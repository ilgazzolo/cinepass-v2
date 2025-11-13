package com.api.boleteria.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MovieUpdateRequestDTO {

    private String title;
    private Boolean adult;
    private String posterUrl;
    private String bannerUrl;
    private String overview;
    private List<String> genres;

}