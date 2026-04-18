package com.rootcause.repository;

import com.rootcause.entity.AnalysisRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Repository for persisted analysis records.
 */
public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecordEntity, UUID>,
        JpaSpecificationExecutor<AnalysisRecordEntity> {

    /**
     * Returns all analysis records ordered by analysis timestamp descending.
     *
     * @return all analysis records sorted from newest to oldest
     */
    List<AnalysisRecordEntity> findAllByOrderByAnalyzedAtDesc();

    /**
     * Returns grouped counts by category ordered by count descending and value ascending.
     *
     * @return grouped counts by category
     */
    @Query("""
            select ar.category as value, count(ar) as count
            from AnalysisRecordEntity ar
            group by ar.category
            order by count(ar) desc, ar.category asc
            """)
    List<AnalysisGroupedCountProjection> countGroupedByCategory();

    /**
     * Returns grouped counts by severity ordered by count descending and value ascending.
     *
     * @return grouped counts by severity
     */
    @Query("""
            select ar.severity as value, count(ar) as count
            from AnalysisRecordEntity ar
            group by ar.severity
            order by count(ar) desc, ar.severity asc
            """)
    List<AnalysisGroupedCountProjection> countGroupedBySeverity();
}