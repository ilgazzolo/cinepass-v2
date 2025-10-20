package com.api.boleteria.repository;

import com.api.boleteria.model.Cinema;
import com.api.boleteria.model.enums.ScreenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICinemaRepository extends JpaRepository<Cinema, Long> {
    List<Cinema> findByScreenType(ScreenType screenType);
    List<Cinema> findByEnabled(boolean enabled);
    List<Cinema> findBySeatCapacityGreaterThan(Integer seatCapacity);
    boolean existsByName(String name); //
    boolean existsByNameAndIdNot(String name, Long id); //
}