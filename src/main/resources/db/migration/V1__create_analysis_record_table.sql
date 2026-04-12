CREATE TABLE analysis_record (
                                 id UUID PRIMARY KEY,
                                 input_text TEXT NOT NULL,
                                 category VARCHAR(100) NOT NULL,
                                 severity VARCHAR(50) NOT NULL,
                                 probable_cause TEXT NOT NULL,
                                 detected_patterns TEXT NOT NULL,
                                 recommended_steps TEXT NOT NULL,
                                 confidence NUMERIC(5,2) NOT NULL,
                                 analyzed_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_analysis_record_category
    ON analysis_record(category);

CREATE INDEX idx_analysis_record_analyzed_at
    ON analysis_record(analyzed_at DESC);