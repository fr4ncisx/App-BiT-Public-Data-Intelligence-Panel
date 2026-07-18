-- Seed data for network_indicators table
-- 27 regions x 5 indicator types x 3 periods = 405 rows
-- Uses CTEs to dynamically resolve region_id and source_id at migration time

WITH network_source AS (
    INSERT INTO data_sources (id, source_name, file_name, source_type, description)
    VALUES (
        uuid_generate_v7(),
        'Network Indicators Seed',
        'network_indicators_seed.csv',
        'SEED_DATA',
        'Indicadores de red semilla para conectividad, cobertura, calidad, congestion y tasa de perdida.'
    )
    ON CONFLICT (file_name, source_type) DO UPDATE SET source_name = EXCLUDED.source_name
    RETURNING id AS source_id
),
region_codes (code) AS (
    VALUES
        ('REG_CBD_BEIRAMAR'),
        ('REG_CENTRO_HISTORICO'),
        ('REG_TRINDADE'),
        ('REG_UFSC'),
        ('REG_COQUEIROS'),
        ('REG_ESTREITO_CAPOEIRAS'),
        ('REG_AEROPORTO_HLZ'),
        ('REG_CAMPECHE'),
        ('REG_LAGOA_CONCEICAO'),
        ('REG_JURERE'),
        ('REG_CANASVIEIRAS'),
        ('REG_INGLESES'),
        ('REG_NORTE_ILHA'),
        ('REG_RESIDENCIAL_NORTE'),
        ('REG_SC401_CORREDOR'),
        ('REG_SAO_JOSE_CENTRO'),
        ('REG_SAO_JOSE_BARREIROS'),
        ('REG_SAO_JOSE_KOBRASOL'),
        ('REG_SAO_JOSE_ROCADO'),
        ('REG_PALHOCA_CENTRO'),
        ('REG_PALHOCA_PEDRA_BRANCA'),
        ('REG_PALHOCA_BR101_SUL'),
        ('REG_BIGUACU_BR101_NORTE'),
        ('REG_VIA_EXPRESSA_CORREDOR'),
        ('REG_SANTO_AMARO'),
        ('REG_GOV_CELSO_RAMOS'),
        ('REG_ANTONIO_CARLOS')
),
indicators (type, unit, description_template) AS (
    VALUES
        ('CONNECTIVITY_INDEX', 'INDEX', 'Indice de conectividad en la region'),
        ('COVERAGE_INDEX', 'INDEX', 'Indice de cobertura de red en la region'),
        ('QUALITY_INDEX', 'INDEX', 'Indice de calidad de servicio en la region'),
        ('CONGESTION_INDEX', 'PERCENTAGE', 'Indice de congestion de red en la region'),
        ('DROP_RATE_INDEX', 'PERCENTAGE', 'Tasa de perdida de conexion en la region')
),
periods (period) AS (
    VALUES ('MANHA'), ('TARDE'), ('NOITE')
)
INSERT INTO network_indicators (id, region_id, source_id, indicator_type, score, unit, gap_level, confidence_level, period, description)
SELECT
    uuid_generate_v7(),
    r.id,
    ns.source_id,
    i.type,
    ROUND(CASE
        WHEN i.type = 'CONNECTIVITY_INDEX' THEN 0.55 + (abs(hashtext(r.region_code)) % 400) / 1000.0
        WHEN i.type = 'COVERAGE_INDEX' THEN 0.50 + (abs(hashtext(r.region_code || i.type)) % 450) / 1000.0
        WHEN i.type = 'QUALITY_INDEX' THEN 0.40 + (abs(hashtext(r.region_code || i.type || p.period)) % 500) / 1000.0
        WHEN i.type = 'CONGESTION_INDEX' THEN 0.15 + (abs(hashtext(r.region_code || i.type)) % 600) / 1000.0
        ELSE 0.10 + (abs(hashtext(r.region_code || i.type || p.period)) % 500) / 1000.0
    END, 4),
    i.unit,
    CASE
        WHEN i.type IN ('CONNECTIVITY_INDEX', 'COVERAGE_INDEX', 'QUALITY_INDEX') THEN
            CASE WHEN abs(hashtext(r.region_code || i.type)) % 100 < 30 THEN 'HIGH'
                 WHEN abs(hashtext(r.region_code || i.type)) % 100 < 70 THEN 'MEDIUM'
                 ELSE 'LOW' END
        ELSE
            CASE WHEN abs(hashtext(r.region_code || i.type)) % 100 < 20 THEN 'HIGH'
                 WHEN abs(hashtext(r.region_code || i.type)) % 100 < 60 THEN 'MEDIUM'
                 ELSE 'LOW' END
    END,
    CASE
        WHEN abs(hashtext(r.region_code || i.type)) % 100 < 40 THEN 'HIGH'
        WHEN abs(hashtext(r.region_code || i.type)) % 100 < 80 THEN 'MEDIUM'
        ELSE 'LOW'
    END,
    p.period,
    i.description_template || ' (' || REPLACE(r.region_code, 'REG_', '') || ')'
FROM regions r
CROSS JOIN indicators i
CROSS JOIN periods p
CROSS JOIN network_source ns
ON CONFLICT (region_id, source_id, indicator_type, period) DO NOTHING;
