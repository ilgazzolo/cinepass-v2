package com.api.boleteria.model;

import com.api.boleteria.model.enums.CardType;
import jakarta.persistence.*;
import lombok.*;



@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String cardNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String cardholderName;

    @Column(nullable = false)
    private String expirationDate;

    @Column(nullable = false)
    private String  issueDate;

    @Column(nullable = false)
    private String cvv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType cardType;

    private Double balance;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}