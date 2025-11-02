package com.api.boleteria.service;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.api.boleteria.dto.detail.MovieDetailDTO;

import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Servicio para gestionar operaciones relacionadas con Peliculas.
 */
@Service
@RequiredArgsConstructor
public class MovieService {


    //-------------- API ----------------//

    private final String token = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIwY2NjYzU3YTFmN2VjZmM1ZjQ4OTNjYTRjOTVmMGU2YyIsIm5iZiI6MTc2MTU2ODIzMy40NDMsInN1YiI6IjY4ZmY2NWU5MmNiMjFmOTMwYWJjMDMxYiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.790N2rYi0hHOa4a3FOeC8bXhTZehMfJBu4n6cjcdLsE";
    private final String baseUrl = "https://api.themoviedb.org/3";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Response makeRequest(String endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        return client.newCall(request).execute();
    }


    public MovieDetailDTO getMovieById(Long id) throws IOException {
        try (Response response = makeRequest("/movie/" + id + "?language=es-ES")) {
            if (!response.isSuccessful()) throw new IOException("Error HTTP " + response.code());
            String json = response.body().string();
            return parseMovieDetailDto(json);
        }

    }

    private MovieDetailDTO parseMovieDetailDto(String json) throws IOException {
        JsonNode node = objectMapper.readTree(json);

        // Extraer los campos directamente
        Long id = node.get("id").asLong();
        String title = node.get("title").asText();
        String originalLanguage = node.get("original_language").asText();
        String releaseDate = node.get("release_date").asText();
        int runtime = node.get("runtime").asInt();
        String overview = node.get("overview").asText();
        String imdbId = node.hasNonNull("imdb_id") ? node.get("imdb_id").asText() : null;
        double voteAverage = node.get("vote_average").asDouble();
        int voteCount = node.get("vote_count").asInt();

        // Lista de géneros (TMDB devuelve un array de objetos con {id, name})
        List<String> genres = new ArrayList<>();
        if (node.has("genres")) {
            for (JsonNode genreNode : node.get("genres")) {
                genres.add(genreNode.get("name").asText());
            }
        }

        // URLs de poster y banner
        String posterPath = node.hasNonNull("poster_path") ? node.get("poster_path").asText() : null;
        String bannerPath = node.hasNonNull("backdrop_path") ? node.get("backdrop_path").asText() : null;

        String posterUrl = posterPath != null ? "https://image.tmdb.org/t/p/w500" + posterPath : null;
        String bannerUrl = bannerPath != null ? "https://image.tmdb.org/t/p/w780" + bannerPath : null;

        return new MovieDetailDTO(
                id,
                title,
                originalLanguage,
                releaseDate,
                runtime,
                genres,
                overview,
                imdbId,
                voteAverage,
                voteCount,
                posterUrl,
                bannerUrl
        );
    }





    //-------------------------------FIND--------------------------------//

    /**
     * Muestra todas las películas asociadas a un género en específico.
     *
     * @param genre género de la película a mostrar.
     * @return lista de MovieListDTO con la información de las películas encontradas.
     * @throws NotFoundException si no se encontraron películas para el género dado.
     */
    /*
    public List<MovieListDTO> findByMovieGenre(String genre) {
        MovieValidator.validateGenre(genre);
        List<MovieListDTO> list = movieRepository.findByMovieGenre(genre).stream()
                .map(this::mapToListDTO)
                .toList();

        if (list.isEmpty()) {
            throw new NotFoundException("No se encontraron películas para el género: " + genre);
        }

        return list;
    }

    /**
     * Obtiene todas las películas cargadas.
     *
     * @return lista de MovieListDTO con la información de las películas encontradas.
     * @throws NotFoundException si no hay películas cargadas.
     */
    /*
    public List<MovieListDTO> findAll() {
        List<MovieListDTO> list = movieRepository.findAll().stream()
                .map(this::mapToListDTO)
                .toList();

        if (list.isEmpty()) {
            throw new NotFoundException("No hay películas cargadas en el sistema.");
        }

        return list;
    }


    /**
     * obtiene una pelicula segun un ID especificado
     * @param id ID de la pelicula a buscar
     * @return MovieDetail con la informacion de la pelicula encontrada
     * @throws IllegalArgumentException si el ID es nulo o inválido.

    public MovieDetailDTO findById(Long id) {
        MovieValidator.validateId(id);
        Movie m = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La pelicula con ID: " + id + " no fue encontrada."));
        return mapToDetailDTO(m);
    }

     */



    //-------------------------------UPDATE--------------------------------//

    /**
     * Actualiza una película existente según su ID.
     *
     * Valida que el ID sea válido y que los campos del DTO cumplan con las reglas definidas.
     * Además, verifica que no exista otra película con el mismo título para evitar duplicados.
     *
     * @param id  ID de la película a actualizar.
     * @param req DTO con los datos actualizados de la película.
     * @return MovieDetailDTO con la información de la película actualizada.
     * @throws IllegalArgumentException si el ID es nulo o inválido.
     * @throws BadRequestException si ya existe otra película con el mismo título.
     * @throws NotFoundException si no se encuentra la película con el ID proporcionado.
     *//*
    public MovieDetailDTO updateById(Long id, MovieRequestDTO req) {
        MovieValidator.validateId(id);  // Validamos el ID primero
        MovieValidator.validateFields(req);

        String title = req.getTitle().trim();
        boolean titleExistsInOther = movieRepository.existsByTitleAndIdNot(title, id);
        if (titleExistsInOther) {
            throw new BadRequestException("Ya existe una película con el título: " + title);
        }

        return movieRepository.findById(id)
                .map(movie -> {
                    movie.setTitle(req.getTitle());
                    movie.setDuration(req.getDuration());
                    movie.setMovieGenre(req.getGenre());
                    movie.setDirector(req.getDirector());
                    movie.setClassification(req.getClassification());
                    movie.setSynopsis(req.getSynopsis());

                    Movie updated = movieRepository.save(movie);
                    return mapToDetailDTO(updated);
                })
                .orElseThrow(() -> new NotFoundException("La película con ID: " + id + " no fue encontrada."));
    }



    //-------------------------------DELETE--------------------------------//

    /**
     * elimina una pelicula segun un ID especificado
     * @param id ID de la pelicula a eliminar
     *//*
    public void deleteById(Long id) {
        MovieValidator.validateId(id);
        if (!movieRepository.existsById(id)) {
            throw new NotFoundException("La pelicula con ID: " + id + " no fue encontrada.");
        }
        movieRepository.deleteById(id);
    }



    //-------------------------------MAP--------------------------------//

    /**
     * Convierte una entidad Movie en un DTO detallado.
     * @param movie entidad Movie
     * @return MovieDetailDTO con todos los datos de la película
     *//*
    private MovieDetailDTO mapToDetailDTO(Movie movie) {
        return new MovieDetailDTO(
                movie.getId(),
                movie.getTitle(),
                movie.getDuration(),
                movie.getMovieGenre(),
                movie.getDirector(),
                movie.getClassification(),
                movie.getSynopsis()
        );
    }
*/
    /**
     * Convierte una entidad Movie en un DTO de lista.
     * @param movie entidad Movie
     * @return MovieListDTO con datos resumidos de la película
     *//*
    private MovieListDTO mapToListDTO(Movie movie) {
        return new MovieListDTO(
                movie.getId(),
                movie.getTitle(),
                movie.getDuration(),
                movie.getMovieGenre(),
                movie.getDirector()
        );
    }

    private Movie mapToEntity(MovieRequestDTO dto) {
        Movie movie = new Movie();
        movie.setTitle(dto.getTitle().trim());
        movie.setDuration(dto.getDuration());
        movie.setMovieGenre(dto.getGenre());
        movie.setDirector(dto.getDirector());
        movie.setClassification(dto.getClassification());
        movie.setSynopsis(dto.getSynopsis());
        return movie;
    }



    //-------------------------------VERIFY--------------------------------//

    /**
     * Verifica si ya existe una película con el título especificado.
     *
     * Realiza una validación previa para asegurarse de que el título no sea nulo ni esté vacío.
     *
     * @param title Título de la película a verificar.
     * @return true si existe una película con ese título, false en caso contrario.
     * @throws IllegalArgumentException si el título es nulo o está vacío.
     *//*
    public boolean existsByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío.");
        }
        return movieRepository.existsByTitle(title.trim());
    }*/
}






