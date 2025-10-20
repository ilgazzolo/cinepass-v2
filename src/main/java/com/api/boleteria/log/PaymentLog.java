package com.api.boleteria.log;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class PaymentLog {
    @Id
    private Long id;
    private String status;
    private String mpOperationId;
    private String userEmail;
    private LocalDateTime timestamp;
    private String error;
}
