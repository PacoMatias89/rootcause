package com.rootcause.repository;

import com.rootcause.entity.AnalysisRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecordEntity, UUID> {
}