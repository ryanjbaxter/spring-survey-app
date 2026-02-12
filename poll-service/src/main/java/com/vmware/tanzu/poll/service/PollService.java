package com.vmware.tanzu.poll.service;

import com.vmware.tanzu.poll.event.PollSubmittedEvent;
import com.vmware.tanzu.poll.model.PollQuestions;
import com.vmware.tanzu.poll.model.PollResponse;
import com.vmware.tanzu.poll.repository.PollResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PollService {

    private static final Logger log = LoggerFactory.getLogger(PollService.class);
    private static final String POLL_SUBMITTED_BINDING = "pollSubmitted-out-0";

    private final PollResponseRepository repository;
    private final StreamBridge streamBridge;
    private final PollQuestions pollQuestions;

    public PollService(PollResponseRepository repository, 
                      StreamBridge streamBridge,
                      PollQuestions pollQuestions) {
        this.repository = repository;
        this.streamBridge = streamBridge;
        this.pollQuestions = pollQuestions;
    }

    public PollResponse submitPoll(PollResponse response) {
        log.info("Received poll response: {} -> {}", response.getQuestionId(), response.getAnswer());
        
        // Save to database
        PollResponse saved = repository.save(response);
        
        // Publish event to Stream
        PollSubmittedEvent event = new PollSubmittedEvent(
            saved.getQuestionId(),
            saved.getAnswer(),
            saved.getTimestamp(),
            saved.getUserId()
        );
        
        streamBridge.send(POLL_SUBMITTED_BINDING, event);
        log.info("Published PollSubmittedEvent to stream");
        
        return saved;
    }

    public List<PollQuestions.Question> getQuestions() {
        return pollQuestions.getQuestions();
    }

    public List<PollResponse> getResponsesByQuestion(String questionId) {
        return repository.findByQuestionId(questionId);
    }
}
