package com.api.boleteria.controller;

import com.api.boleteria.dto.detail.MovieDetailDTO;
import com.api.boleteria.dto.list.MovieListDTO;
import com.api.boleteria.model.MovieCartelera;
import com.api.boleteria.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de películas.
 *
 * Permite crear, obtener, actualizar y eliminar películas, así como
 * buscar películas por género.
 *
 * Las operaciones de creación, actualización y eliminación están restringidas
 * a usuarios con rol ADMIN, mientras que la consulta está permitida también
 * para usuarios con rol CLIENT.
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            MovieDetailDTO movie = movieService.getMovieById(id);
            if (movie == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("La película con id " + id + " no fue encontrada");
            }
            return ResponseEntity.ok(movie);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Error al obtener los datos de la película: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public List<MovieDetailDTO> searchMovies(@RequestParam String title) throws IOException {
        return movieService.searchMoviesByTitle(title);
    }

    @PostMapping("/save/{id}")
    public ResponseEntity<?> saveMovie(@PathVariable Long id) {
        try {
            MovieCartelera savedMovie = movieService.save(id);
            if (savedMovie == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontró la película con id " + id);
            }
            return ResponseEntity.ok(savedMovie);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar la película: " + e.getMessage());
        }
    }

    @GetMapping("")
    public ResponseEntity<List<MovieCartelera>> getAllMovies() {
        List<MovieCartelera> movies = movieService.getAllMovies();
        if (movies.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(movies);
    }


    @DeleteMapping("delete/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        movieService.deleteById(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Película eliminada correctamente.");

        return ResponseEntity.ok(response);
    }













    //-------------------------------GET--------------------------------//
    /**
     * Obtiene la lista de todas las películas.
     *
     */
/*
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<List<MovieListDTO>> getAll() {
        List<MovieListDTO> movieList = movieService.findAll();
        return ResponseEntity.ok(movieList);
    }

    /**
     * Obtiene el detalle de una película específica por su ID.
     *
     * @param id Identificador de la película.
     * @return ResponseEntity con el detalle de la película.
     */
/*
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<MovieDetailDTO> getById(@PathVariable String id){
        return ResponseEntity.ok(movieService.getMovieById(id));
    }*/

    /**
     * Obtiene una lista de películas que coinciden con el género especificado.
     * <p>
     *
     * @param genre Género de las películas a buscar.
     * @return ResponseEntity con la lista de MovieListDTO si hay resultados,
     * o un 204 No Content si la lista está vacía.
     *//*
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieListDTO>> findByGenre(@PathVariable String genre) {
        List<MovieListDTO> movies = movieService.findByMovieGenre(genre);
        return ResponseEntity.ok(movies);
    }


    //-------------------------------UPDATE--------------------------------//

    /**
     * Actualiza una película por su ID.
     *
     * @param id Identificador de la película a actualizar.
     * @param entity DTO con la nueva información para la película.
     * @return ResponseEntity con el detalle actualizado de la película.
     */
/*
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDetailDTO> update(@PathVariable Long id, @Valid @RequestBody MovieRequestDTO entity){
        return ResponseEntity.ok(movieService.updateById(id, entity));
    }



    //-------------------------------DELETE--------------------------------//

    /**
     * Elimina una película por su ID.
     *
     * @param id Identificador de la película a eliminar.
     * @return ResponseEntity con estado 200 OK y un mensaje confirmando la eliminación exitosa.
     *//*
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        movieService.deleteById(id);
        return ResponseEntity.ok("Tu película fue eliminada correctamente");
    }*/
}
