package io.spring.sample.results.controller;

import io.spring.sample.results.model.PollResults;
import io.spring.sample.results.service.ResultsAggregator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class ResultsController {

    private final ResultsAggregator resultsAggregator;

    public ResultsController(ResultsAggregator resultsAggregator) {
        this.resultsAggregator = resultsAggregator;
    }

    @GetMapping("/{questionId}")
    public PollResults getResults(@PathVariable String questionId) {
        return resultsAggregator.getResults(questionId)
            .orElse(new PollResults(questionId));
    }
}
