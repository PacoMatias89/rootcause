package com.rootcause.exception;

import java.util.UUID;

/**
 * Exception thrown when a requested analysis cannot be found.
 *
 * <p>This exception is used when the application attempts to retrieve a persisted
 * analysis by its identifier and no matching record exists.</p>
 *
 * <p>It is intended to be translated into an HTTP 404 Not Found response by the
 * global exception handling layer.</p>
 */
public class AnalysisNotFoundException extends RuntimeException {

    /**
     * Creates the exception for the missing analysis identifier.
     *
     * @param analysisId identifier of the analysis that was not found
     */
    public AnalysisNotFoundException(final UUID analysisId) {
        super("Analysis " + analysisId + " not found");
    }
}