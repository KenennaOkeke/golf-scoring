package com.example.tournamentservice.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Entity
@Table(name = "plays")
public class PlayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Setter
    private String tournamentId;

    @Setter
    private String name;

    @Setter
    private int score;

    @Setter
    private int hole;
}
