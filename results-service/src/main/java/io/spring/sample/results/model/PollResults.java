package io.spring.sample.results.model;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "poll_results")
public class PollResults {

    @Id
    private String questionId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "answer_counts", joinColumns = @JoinColumn(name = "question_id"))
    @MapKeyColumn(name = "answer")
    @Column(name = "count")
    private Map<String, Integer> answerCounts = new HashMap<>();

    private int totalResponses;

    public PollResults() {
    }

    public PollResults(String questionId) {
        this.questionId = questionId;
        this.totalResponses = 0;
    }

    public void incrementAnswer(String answer) {
        answerCounts.merge(answer, 1, Integer::sum);
        totalResponses++;
    }

    // Getters and setters
    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public Map<String, Integer> getAnswerCounts() {
        return answerCounts;
    }

    public void setAnswerCounts(Map<String, Integer> answerCounts) {
        this.answerCounts = answerCounts;
    }

    public int getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(int totalResponses) {
        this.totalResponses = totalResponses;
    }
}
