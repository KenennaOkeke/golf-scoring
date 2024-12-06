package com.example.tournamentservice;

import com.example.tournamentservice.component.Course;
import com.example.tournamentservice.controller.PlayerController;
import com.example.tournamentservice.controller.ViewerController;
import com.example.tournamentservice.jpa.repositories.PlayRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class EndToEndTest {
    Logger logger = LoggerFactory.getLogger(PlayerController.class);

    final PlayRepository playRepository;
    final Course course;

    final MockMvc mockMvc;

    @Autowired
    public EndToEndTest(PlayRepository playRepository, Course course) {
        this.playRepository = playRepository;
        this.course = course;
        this.mockMvc = MockMvcBuilders.standaloneSetup(
                new PlayerController(this.playRepository, this.course),
                new ViewerController(this.playRepository, this.course)
        ).setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();
    }

    @BeforeEach
    public void setUp() {
        playRepository.deleteAll();
    }

    @AfterAll
    public static void tearDown(@Autowired PlayRepository playRepository) {
        playRepository.deleteAll();
    }

    @Test
    public void simulateTournament() throws Exception {
        String tournamentId = UUID.randomUUID().toString();
        List<Integer> playerOneScores = generatePlayerScores();
        List<Integer> playerTwoScores = generatePlayerScores();
        List<Integer> playerThreeScores = generatePlayerScores();
        List<Integer> playerFourScores = generatePlayerScores();
        List<Integer> playerFiveScores = generatePlayerScores();

        processPlayerScores("Player One", tournamentId, playerOneScores);
        processPlayerScores("Player Two", tournamentId, playerTwoScores);
        processPlayerScores("Player Three", tournamentId, playerThreeScores);
        processPlayerScores("Player Four", tournamentId, playerFourScores);
        processPlayerScores("Player Five", tournamentId, playerFiveScores);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/viewer/leaderboard")
                        .param("tournamentId", tournamentId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]['Player One'].score", contains(determinePlayerFinalScore(playerOneScores))))
                .andExpect(jsonPath("$[*]['Player Two'].score", contains(determinePlayerFinalScore(playerTwoScores))))
                .andExpect(jsonPath("$[*]['Player Three'].score", contains(determinePlayerFinalScore(playerThreeScores))))
                .andExpect(jsonPath("$[*]['Player Four'].score", contains(determinePlayerFinalScore(playerFourScores))))
                .andExpect(jsonPath("$[*]['Player Five'].score", contains(determinePlayerFinalScore(playerFiveScores))))
                .andReturn();

        logger.info("Leaderboard: {}", result.getResponse().getContentAsString());
    }

    private void putScore(String tournamentId, String name, int hole, int score) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/player/score")
                        .param("tournamentId", tournamentId)
                        .param("name", name)
                        .param("hole", String.valueOf(hole))
                        .param("score", String.valueOf(score))
                ) // Verify expectations before returning, enabling future reuse of method for unit tests
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tournamentId", is(tournamentId)))
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.score", is(score)))
                .andExpect(jsonPath("$.hole", is(hole)));
    }

    private void processPlayerScores(String name, String tournamentId, List<Integer> scores) throws Exception {
        for (int i = 0; i < scores.size(); i++) {
            putScore(tournamentId, name, i + 1, scores.get(i));
        }
    }

    private List<Integer> generatePlayerScores() {
        return new Random().ints(18, 1, 8).boxed().toList();
    }

    private String determinePlayerFinalScore(List<Integer> scores) {
        Integer totalScore = 0;
        for (int i = 0; i < scores.size(); i++) {
            int holePar = course.getPars().get(i);
            totalScore += scores.get(i) - holePar;
        }
        return totalScore.equals(0) ? "E" : totalScore.toString();
    }

}
