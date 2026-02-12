package com.vmware.tanzu.results.repository;

import com.vmware.tanzu.results.model.PollResults;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollResultsRepository extends JpaRepository<PollResults, String> {
}
