package com.vmware.tanzu.poll.repository;

import com.vmware.tanzu.poll.model.PollResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PollResponseRepository extends JpaRepository<PollResponse, Long> {
    
    List<PollResponse> findByQuestionId(String questionId);
    
}
