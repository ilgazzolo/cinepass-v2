package com.api.boleteria.model;

import com.api.boleteria.model.enums.ScreenType;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cinemas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cinema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ScreenType screenType;

    @Column(nullable = false)
    private Boolean atmos;

    @Column(nullable = false)
    private int rows;

    @Column(nullable = false)
    private int columns;

    private Integer seatCapacity;

    @Column(nullable = false)
    private Boolean enabled;

    @OneToMany(mappedBy = "cinema", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Function> functions = new ArrayList<>();

    // Método auxiliar
    public void calculateSeatCapacity() {
        this.seatCapacity = this.rows * this.columns;
    }

}
