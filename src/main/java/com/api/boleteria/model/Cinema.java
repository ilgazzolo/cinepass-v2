package com.api.boleteria.model;

import com.api.boleteria.model.enums.ScreenType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cinemas")
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
    private Integer seatCapacity;

    @Column(nullable = false)
    private Boolean enabled;

    @OneToMany(mappedBy = "cinema", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Function> functions = new ArrayList<>();



}
