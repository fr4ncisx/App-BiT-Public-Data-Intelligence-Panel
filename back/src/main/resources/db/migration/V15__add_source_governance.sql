ALTER TABLE data_sources
    ADD COLUMN confidence_level VARCHAR(16),
    ADD COLUMN period_start VARCHAR(16),
    ADD COLUMN period_end VARCHAR(16),
    ADD COLUMN governance_type VARCHAR(16);

ALTER TABLE data_sources
    ADD CONSTRAINT ck_data_sources_confidence_level
        CHECK (confidence_level IN ('LOW', 'MEDIUM', 'HIGH'));

ALTER TABLE data_sources
    ADD CONSTRAINT ck_data_sources_governance_type
        CHECK (governance_type IN ('PUBLIC', 'SEED', 'INFERRED'));
