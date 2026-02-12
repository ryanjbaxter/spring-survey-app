package com.vmware.tanzu.ui.model;

import java.util.List;

public record PollQuestion(
    String id,
    String text,
    List<String> answers
) {
}
