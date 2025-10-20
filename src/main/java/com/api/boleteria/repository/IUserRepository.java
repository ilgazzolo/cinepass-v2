package com.api.boleteria.repository;

import com.api.boleteria.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername (String username);
    Boolean existsByUsername (String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsernameAndIdNot(String username, Long id); //
    boolean existsByEmailAndIdNot(String email, Long id); //
}