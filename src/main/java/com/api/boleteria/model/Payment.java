package com.api.boleteria.model;

import com.api.boleteria.model.enums.StatusPayment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // ID de pago que devuelve Mercado Pago
    @Column(nullable = false, unique = true)
    private String mpPaymentId;

    // Estado del pago (approved, pending, failure)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPayment status;

    // Monto del pago
    private Double amount;

    // Fecha de creación o actualización del pago
    private LocalDateTime date;

    // Relación con el ticket asociado (1 pago -> 1 ticket)
    @OneToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
}
