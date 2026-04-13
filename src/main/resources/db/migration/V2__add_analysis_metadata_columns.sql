ALTER TABLE analysis_record
    ADD COLUMN rule_code VARCHAR(100);

ALTER TABLE analysis_record
    ADD COLUMN raw_input_length INTEGER;

ALTER TABLE analysis_record
    ADD COLUMN matched_rule_count INTEGER;

UPDATE analysis_record
SET rule_code = 'UNKNOWN_RULE'
WHERE rule_code IS NULL;

UPDATE analysis_record
SET raw_input_length = CHAR_LENGTH(input_text)
WHERE raw_input_length IS NULL;

UPDATE analysis_record
SET matched_rule_count = CASE
                             WHEN category = 'UNKNOWN' THEN 0
                             ELSE 1
    END
WHERE matched_rule_count IS NULL;

ALTER TABLE analysis_record
    ALTER COLUMN rule_code SET NOT NULL;

ALTER TABLE analysis_record
    ALTER COLUMN raw_input_length SET NOT NULL;

ALTER TABLE analysis_record
    ALTER COLUMN matched_rule_count SET NOT NULL;

CREATE INDEX idx_analysis_record_rule_code
    ON analysis_record(rule_code);