package com.api.boleteria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movies_cartelera")
public class MovieCartelera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String title;

    @Column(nullable = true)
    private String originalLanguage;

    @Column(nullable = true)
    private String releaseDate;

    @Column(nullable = true)
    private Integer runtime;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String overview;

    @Column(nullable = true)
    private String imdbId;

    @Column(nullable = true)
    private Double voteAverage;

    @Column(nullable = true)
    private Integer voteCount;

    @Column(nullable = true)
    private String posterUrl;

    @Column(nullable = true)
    private String bannerUrl;
}
