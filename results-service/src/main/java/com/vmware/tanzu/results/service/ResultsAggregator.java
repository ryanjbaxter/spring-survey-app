package com.vmware.tanzu.results.service;

import com.vmware.tanzu.results.event.PollSubmittedEvent;
import com.vmware.tanzu.results.event.ResultsUpdatedEvent;
import com.vmware.tanzu.results.model.PollResults;
import com.vmware.tanzu.results.repository.PollResultsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class ResultsAggregator {

    private static final Logger log = LoggerFactory.getLogger(ResultsAggregator.class);
    private static final String RESULTS_UPDATED_BINDING = "resultsUpdated-out-0";

    private final PollResultsRepository repository;
    private final StreamBridge streamBridge;
    private final Sinks.Many<ResultsUpdatedEvent> resultsSink;

    public ResultsAggregator(PollResultsRepository repository, StreamBridge streamBridge) {
        this.repository = repository;
        this.streamBridge = streamBridge;
        this.resultsSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @Bean
    public Consumer<PollSubmittedEvent> pollSubmitted() {
        return event -> {
            log.info("Received PollSubmittedEvent: {} -> {}", event.questionId(), event.answer());
            
            // Find or create results
            PollResults results = repository.findById(event.questionId())
                .orElse(new PollResults(event.questionId()));
            
            // Increment the count
            results.incrementAnswer(event.answer());
            
            // Save updated results
            PollResults saved = repository.save(results);
            log.info("Updated results for {}: {} total responses", 
                    saved.getQuestionId(), saved.getTotalResponses());
            
            // Publish updated results event
            ResultsUpdatedEvent updateEvent = new ResultsUpdatedEvent(
                saved.getQuestionId(),
                saved.getAnswerCounts(),
                saved.getTotalResponses(),
                Instant.now()
            );
            
            streamBridge.send(RESULTS_UPDATED_BINDING, updateEvent);
            resultsSink.tryEmitNext(updateEvent);
            log.info("Published ResultsUpdatedEvent");
        };
    }

    public Optional<PollResults> getResults(String questionId) {
        return repository.findById(questionId);
    }

    public Flux<ResultsUpdatedEvent> streamResults() {
        return resultsSink.asFlux();
    }
}
