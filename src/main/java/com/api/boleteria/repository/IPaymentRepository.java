package com.api.boleteria.repository;

import com.api.boleteria.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMpPaymentId(String mpPaymentId);
}
