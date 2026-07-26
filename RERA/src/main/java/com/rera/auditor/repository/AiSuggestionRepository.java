package com.rera.auditor.repository;

import com.rera.auditor.entity.AiSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiSuggestionRepository extends JpaRepository<AiSuggestion, Long> {
    List<AiSuggestion> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
