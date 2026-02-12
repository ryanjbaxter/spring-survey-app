package com.vmware.tanzu.poll.event;

import java.time.Instant;

public record PollSubmittedEvent(
    String questionId,
    String answer,
    Instant timestamp,
    String userId
) {
}
