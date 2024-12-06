package com.example.tournamentservice.controller;

import com.example.tournamentservice.component.Course;
import com.example.tournamentservice.jpa.entities.PlayEntity;
import com.example.tournamentservice.jpa.repositories.PlayRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/player")
public class PlayerController {
    Logger logger = LoggerFactory.getLogger(PlayerController.class);

    private final PlayRepository playRepository;
    private final Course course;

    @Autowired
    public PlayerController(PlayRepository playRepository, Course course) {
        this.playRepository = playRepository;
        this.course = course;
    }

    @PutMapping("/score")
    public ResponseEntity<PlayEntity> putScore(
            @RequestParam String tournamentId,
            @RequestParam @NotBlank String name,
            @RequestParam @NotNull @Min(1) @Max(18) Integer hole,
            @RequestParam @NotNull Integer score
    ) {
        logger.info("Received play with name {} and score {} and hole {} for tournament {}", name, score, hole, tournamentId);

        PlayEntity play = playRepository.findByTournamentIdAndNameAndHole(tournamentId, name, hole);

        if (play == null) {  // TODO: create or update on conflict
            play = new PlayEntity();
            play.setTournamentId(tournamentId);
            play.setName(name);
            play.setHole(hole);
            play.setScore(score);
            play = playRepository.save(play);
            logger.info("Created play {}", play.getId());
        } else {
            logger.info("Found play {} with name {} and score {} and hole {} in tournament {}", play.getId(), name, score, hole, tournamentId);
            play.setScore(score);
            playRepository.save(play);
            logger.info("Updated play {}; updated score to {} from {}", play.getId(), score, play.getScore());
        }

        //Aynsc POST/SNS TO mobile-service(s) to update leaderboard

        int calculatedScore = play.getScore() - course.getPars().get(play.getHole() - 1);
        if(calculatedScore <= -1) {
            //Aynsc POST/SNS TO mobile-service; a <= -1 notification
        }

        return new ResponseEntity<>(play, HttpStatus.OK);
    }

}
