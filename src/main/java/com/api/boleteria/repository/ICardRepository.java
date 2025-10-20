package com.api.boleteria.repository;

import com.api.boleteria.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByUserId(Long userId);
    boolean existsByCardNumber(String cardNumber); //
    boolean existsByCardNumberAndIdNot(String cardNumber, Long id); //
}