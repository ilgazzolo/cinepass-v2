package com.api.boleteria.service;

import com.api.boleteria.dto.detail.MovieDetailDTO;
import com.api.boleteria.dto.list.MovieListDTO;
import com.api.boleteria.dto.request.MovieRequestDTO;
import com.api.boleteria.exception.BadRequestException;
import com.api.boleteria.exception.NotFoundException;
import com.api.boleteria.model.Function;
import com.api.boleteria.model.Movie;
import com.api.boleteria.repository.IFunctionRepository;
import com.api.boleteria.repository.IMovieRepository;
import com.api.boleteria.validators.MovieValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar operaciones relacionadas con Peliculas.
 */
@Service
@RequiredArgsConstructor
public class MovieService {

    private final IMovieRepository movieRepository;


    //-------------------------------SAVE--------------------------------//

    /**
     * Crea una o más películas a partir de una lista de DTOs.
     *
     * Valida cada película antes de ser persistida, asegurándose de que no existan duplicados por título.
     *
     * @param requests Lista de DTOs con los datos de las películas a crear.
     * @return Lista de MovieDetailDTO con la información de las películas creadas.
     * @throws BadRequestException si alguna película ya existe o los datos son inválidos.
     */
    public List<MovieDetailDTO> createAll(List<MovieRequestDTO> requests) {
        List<Movie> moviesToSave = new ArrayList<>();

        for (MovieRequestDTO req : requests) {
            MovieValidator.validateFields(req);

            String title = req.getTitle().trim();

            if (movieRepository.existsByTitle(title)) {
                throw new BadRequestException("Ya existe una película con el título: " + title);
            }

            moviesToSave.add(mapToEntity(req));
        }

        List<Movie> saved = movieRepository.saveAll(moviesToSave);

        return saved.stream()
                .map(this::mapToDetailDTO)
                .toList();
    }



    //-------------------------------FIND--------------------------------//

    /**
     * Muestra todas las películas asociadas a un género en específico.
     *
     * @param genre género de la película a mostrar.
     * @return lista de MovieListDTO con la información de las películas encontradas.
     * @throws NotFoundException si no se encontraron películas para el género dado.
     */
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
     */
    public MovieDetailDTO findById(Long id) {
        MovieValidator.validateId(id);
        Movie m = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La pelicula con ID: " + id + " no fue encontrada."));
        return mapToDetailDTO(m);
    }



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
     */
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
     */
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
     */
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

    /**
     * Convierte una entidad Movie en un DTO de lista.
     * @param movie entidad Movie
     * @return MovieListDTO con datos resumidos de la película
     */
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
     */
    public boolean existsByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío.");
        }
        return movieRepository.existsByTitle(title.trim());
    }
}






