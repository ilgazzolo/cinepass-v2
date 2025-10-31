package com.api.boleteria.model;

import com.api.boleteria.model.enums.StatusPayment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String mpPaymentId; // ID de Mercado Pago

    @Column(nullable = false)
    private String userEmail;   // Email del usuario que realizó el pago

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPayment status;    // Estado del pago (approved, pending, failure)

    private BigDecimal amount;  // Monto del pago

    private LocalDateTime createdAt;    // Fecha de creación del pago

    private LocalDateTime updatedAt;    // Fecha de actualización del pago

    private LocalDateTime date;

    @OneToOne
    @JoinColumn(name = "ticket_id")    // Relación con el ticket asociado (1 pago -> 1 ticket)
    private Ticket ticket;

}


