package com.vmware.tanzu.results.service;

import com.vmware.tanzu.results.event.PollSubmittedEvent;
import com.vmware.tanzu.results.model.PollResults;
import com.vmware.tanzu.results.repository.PollResultsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;

@Service
public class ResultsAggregator {

    private static final Logger log = LoggerFactory.getLogger(ResultsAggregator.class);

    private final PollResultsRepository repository;

    public ResultsAggregator(PollResultsRepository repository) {
        this.repository = repository;
    }

    @Bean
    public Consumer<PollSubmittedEvent> pollSubmitted() {
        return event -> {
            log.info("Received PollSubmittedEvent: {} -> {}", event.questionId(), event.answer());

            PollResults results = repository.findById(event.questionId())
                .orElse(new PollResults(event.questionId()));

            results.incrementAnswer(event.answer());

            PollResults saved = repository.save(results);
            log.info("Updated results for {}: {} total responses",
                    saved.getQuestionId(), saved.getTotalResponses());
        };
    }

    public Optional<PollResults> getResults(String questionId) {
        return repository.findById(questionId);
    }
}
