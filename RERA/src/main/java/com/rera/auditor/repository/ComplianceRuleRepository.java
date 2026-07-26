package com.rera.auditor.repository;

import com.rera.auditor.entity.ComplianceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ComplianceRuleRepository extends JpaRepository<ComplianceRule, Long> {
    List<ComplianceRule> findByCategory(String category);
    Optional<ComplianceRule> findByRuleCode(String ruleCode);
}
