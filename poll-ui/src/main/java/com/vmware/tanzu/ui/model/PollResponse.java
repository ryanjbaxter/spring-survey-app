package com.vmware.tanzu.ui.model;

public record PollResponse(
    String questionId,
    String answer,
    String userId
) {
}
