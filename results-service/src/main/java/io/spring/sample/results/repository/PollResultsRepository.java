package io.spring.sample.results.repository;

import io.spring.sample.results.model.PollResults;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollResultsRepository extends JpaRepository<PollResults, String> {
}
