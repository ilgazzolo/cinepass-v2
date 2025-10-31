package com.api.boleteria.log;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PaymentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String status;
    @Column(unique = true)
    private String mpOperationId;
    private String userEmail;
    private LocalDateTime timestamp;
    private String error;
}
