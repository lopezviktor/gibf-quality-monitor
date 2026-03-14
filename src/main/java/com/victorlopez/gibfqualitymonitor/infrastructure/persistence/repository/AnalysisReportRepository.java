package com.victorlopez.gibfqualitymonitor.infrastructure.persistence.repository;

import com.victorlopez.gibfqualitymonitor.infrastructure.persistence.entity.AnalysisReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReportEntity, UUID> {
}
