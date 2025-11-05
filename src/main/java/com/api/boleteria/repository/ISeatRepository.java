package com.api.boleteria.repository;

import com.api.boleteria.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ISeatRepository extends JpaRepository<Seat, Long> {
    List<Seat>findByFunctionId(Long functionId);
}
