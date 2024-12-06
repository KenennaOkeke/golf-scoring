package com.example.tournamentservice.jpa.repositories;

import com.example.tournamentservice.jpa.entities.PlayEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayRepository extends JpaRepository<PlayEntity, UUID> {
    @Query("select pe from PlayEntity pe where pe.tournamentId = :tournamentId AND pe.name = :name AND pe.hole = :hole")
    PlayEntity findByTournamentIdAndNameAndHole(
            @Param("tournamentId") String tournamentId,
            @Param("name") String name,
            @Param("hole") int hole
    );

    List<PlayEntity> findAllByTournamentId(String tournamentId);
}
