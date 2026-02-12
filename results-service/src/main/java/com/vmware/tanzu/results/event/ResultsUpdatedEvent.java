package com.vmware.tanzu.results.event;

import java.time.Instant;
import java.util.Map;

public record ResultsUpdatedEvent(
    String questionId,
    Map<String, Integer> answerCounts,
    int totalResponses,
    Instant timestamp
) {
}
