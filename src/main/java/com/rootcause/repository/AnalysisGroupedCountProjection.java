package com.rootcause.repository;

/**
 * Projection used to represent grouped count queries over persisted analyses.
 */
public interface AnalysisGroupedCountProjection {

    /**
     * Returns the grouped value.
     *
     * @return grouped value
     */
    String getValue();

    /**
     * Returns the grouped count.
     *
     * @return grouped count
     */
    long getCount();
}