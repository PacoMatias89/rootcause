package com.rootcause.repository;

import com.rootcause.entity.AnalysisRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

/**
 * Repository for persisted analysis records.
 *
 * <p>This repository provides CRUD access to {@link AnalysisRecordEntity} instances and
 * also supports specification-based queries for filtered and paginated history retrieval.</p>
 */

public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecordEntity, UUID>,
        JpaSpecificationExecutor<AnalysisRecordEntity> {

    /**
     * Retrieves all persisted analyses ordered from newest to oldest.
     *
     * @return list of stored analyses sorted by analysis timestamp descending
     */

    List<AnalysisRecordEntity> findAllByOrderByAnalyzedAtDesc();
}