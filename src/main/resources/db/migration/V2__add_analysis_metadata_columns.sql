alter table analysis_record
    add column rule_code varchar(200);

alter table analysis_record
    add column raw_input_length integer;

alter table analysis_record
    add column matched_input_count integer;

update analysis_record
set rule_code = 'UNKNOWN_RULE'
where rule_code is null;

update analysis_record
set matched_input_count = case
    whem category = 'UNKNOWN' then 0
    else 1
end
where matched_input_count is null;

alter table analysis_record
    alter column rule_code set not null;

alter table analysis_record
    alter column raw_input_length set not null;

alter table analysis_record
    alter column matched_input_count set not null;

create index idx_analysis_record_rule_code on analysis_record(rule_code);