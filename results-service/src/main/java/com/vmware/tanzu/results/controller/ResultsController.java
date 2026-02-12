package com.vmware.tanzu.results.controller;

import com.vmware.tanzu.results.event.ResultsUpdatedEvent;
import com.vmware.tanzu.results.model.PollResults;
import com.vmware.tanzu.results.service.ResultsAggregator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/results")
public class ResultsController {

    private final ResultsAggregator resultsAggregator;

    public ResultsController(ResultsAggregator resultsAggregator) {
        this.resultsAggregator = resultsAggregator;
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<PollResults> getResults(@PathVariable String questionId) {
        return resultsAggregator.getResults(questionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ResultsUpdatedEvent> streamResults() {
        return resultsAggregator.streamResults();
    }
}
