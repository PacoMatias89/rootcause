package com.rootcause.exception;

import java.util.UUID;

/**
 *  Exception for analysis with id not found (404)
 *
 * */

public class AnalysisNotFoundException extends RuntimeException {

    public AnalysisNotFoundException(final UUID analysisId){
        super("Analysis " + analysisId + " not found");
    }
}
