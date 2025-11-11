package com.api.boleteria.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private Boolean adult;

    @Column(nullable = true)
    private String bannerUrl;

    @Column(columnDefinition = "JSON")
    private String genresJson;

    @Transient
    private List<String> genres;


    // ✅ Setter: convierte lista → JSON string
    public void setGenres(List<String> genres) {
        this.genres = genres;
        try {
            this.genresJson = new ObjectMapper().writeValueAsString(genres);
        } catch (Exception e) {
            this.genresJson = "[]";
        }
    }

    // ✅ Getter: convierte JSON string → lista
    public List<String> getGenres() {
        if (genres == null && genresJson != null) {
            try {
                genres = new ObjectMapper().readValue(
                        genresJson, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                genres = List.of();
            }
        }
        return genres;
    }
}
