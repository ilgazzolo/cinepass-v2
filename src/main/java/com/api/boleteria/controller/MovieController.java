package com.api.boleteria.controller;

import com.api.boleteria.dto.detail.MovieDetailDTO;
import com.api.boleteria.dto.list.MovieListDTO;
import com.api.boleteria.dto.request.MovieRequestDTO;
import com.api.boleteria.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@Validated
public class MovieController {

    @Autowired
    private MovieService movieService;


    //-------------------------------CREATE--------------------------------//
    /**
     * Registra una o más películas.
     *
     * Este endpoint permite a un administrador cargar múltiples películas en una sola solicitud.
     * Cada película es validada y registrada si no existe previamente una con el mismo título.
     *
     * @param entity Lista de DTOs con la información necesaria para crear las películas.
     * @return ResponseEntity con la lista de películas creadas.
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MovieDetailDTO>> create(@Valid @RequestBody List<@Valid MovieRequestDTO> entity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createAll(entity));
    }



    //-------------------------------GET--------------------------------//
    /**
     * Obtiene la lista de todas las películas.
     *
     */

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

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<MovieDetailDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(movieService.findById(id));
    }

    /**
     * Obtiene una lista de películas que coinciden con el género especificado.
     * <p>
     *
     * @param genre Género de las películas a buscar.
     * @return ResponseEntity con la lista de MovieListDTO si hay resultados,
     * o un 204 No Content si la lista está vacía.
     */
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
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        movieService.deleteById(id);
        return ResponseEntity.ok("Tu película fue eliminada correctamente");
    }
}
