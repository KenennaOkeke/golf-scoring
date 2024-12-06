package com.example.tournamentservice.controller;

import com.example.tournamentservice.component.Course;
import com.example.tournamentservice.jpa.entities.PlayEntity;
import com.example.tournamentservice.jpa.repositories.PlayRepository;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/viewer")
public class ViewerController {
    Logger logger = LoggerFactory.getLogger(PlayerController.class);

    private final PlayRepository playRepository;
    private final Course course;

    @Autowired
    public ViewerController(PlayRepository playRepository, Course course) {
        this.playRepository = playRepository;
        this.course = course;
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<String> getLeaderboard(
            @RequestParam String tournamentId
    ) {
        logger.info("Received request for leaderboard of tournament {}", tournamentId);

        JSONObject players = new JSONObject();

        List<PlayEntity> plays = playRepository.findAllByTournamentId(tournamentId);
        for (PlayEntity play : plays) {
            int holePar = course.getPars().get(play.getHole() - 1);
            int score = play.getScore() - holePar;
            int previousScore = players.has(play.getName()) ? players.optJSONObject(play.getName()).optInt("score", 0) : 0;
            int holes = players.has(play.getName()) ? players.optJSONObject(play.getName()).optInt("holes", 0) : 0;
            players.put(
                    play.getName(),
                    new JSONObject()
                            .putOpt("score", previousScore + score)
                            .putOpt("holes", holes + 1)
            );
        }

        JSONArray playersSorted = new JSONArray();
        players.keySet().stream()
                .sorted(Comparator.comparingInt(k -> players.getJSONObject(k).getInt("score")))
                .forEach(k -> {
                    JSONObject player = players.getJSONObject(k);
                    String score = player.getInt("score") == 0 ? "E" : String.valueOf(player.getInt("score")); // Stringify score
                    player.put("score", score);
                    playersSorted.put(new JSONObject().put(k, player));
                });
        return ResponseEntity.ok(playersSorted.toString());
    }

}
