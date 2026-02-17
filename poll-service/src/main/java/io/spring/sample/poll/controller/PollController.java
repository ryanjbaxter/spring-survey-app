package io.spring.sample.poll.controller;

import io.spring.sample.poll.model.PollQuestions;
import io.spring.sample.poll.model.PollResponse;
import io.spring.sample.poll.service.PollService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class PollController {

    private static final Logger log = LoggerFactory.getLogger(PollController.class);

    private final PollService pollService;
    
    @Value("${server.port}")
    private int serverPort;
    
    @Value("${spring.application.name}")
    private String applicationName;

    public PollController(PollService pollService) {
        this.pollService = pollService;
    }

    @GetMapping("/questions")
    public ResponseEntity<List<PollQuestions.Question>> getQuestions() {
        log.info("[{}:{}] Fetching poll questions", applicationName, serverPort);
        return ResponseEntity.ok(pollService.getQuestions());
    }

    @PostMapping("/submit")
    public ResponseEntity<PollResponse> submitPoll(@RequestBody PollResponse response) {
        log.info("[{}:{}] Submitting poll response: {} -> {}", 
                applicationName, serverPort, response.getQuestionId(), response.getAnswer());
        PollResponse saved = pollService.submitPoll(response);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/responses/{questionId}")
    public ResponseEntity<List<PollResponse>> getResponses(@PathVariable String questionId) {
        log.info("[{}:{}] Fetching responses for question: {}", 
                applicationName, serverPort, questionId);
        return ResponseEntity.ok(pollService.getResponsesByQuestion(questionId));
    }
}
