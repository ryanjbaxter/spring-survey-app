package io.spring.sample.results.event;

import java.time.Instant;

public record PollSubmittedEvent(
    String questionId,
    String answer,
    Instant timestamp,
    String userId
) {
}
