package com.vmware.tanzu.poll.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "poll_responses")
public class PollResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String questionId;

    @Column(nullable = false)
    private String answer;

    @Column(nullable = false)
    private Instant timestamp;

    private String userId;

    public PollResponse() {
        this.timestamp = Instant.now();
    }

    public PollResponse(String questionId, String answer, String userId) {
        this();
        this.questionId = questionId;
        this.answer = answer;
        this.userId = userId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
