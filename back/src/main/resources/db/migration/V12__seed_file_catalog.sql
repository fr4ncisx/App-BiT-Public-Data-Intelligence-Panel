CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION uuid_generate_v7() RETURNS UUID AS $$
DECLARE
    unix_ts_ms BYTEA;
    uuid_bytes BYTEA;
BEGIN
    unix_ts_ms = substring(
        int8send(floor(extract(epoch FROM clock_timestamp()) * 1000)::bigint)
        FROM 3
    );

    uuid_bytes = unix_ts_ms || gen_random_bytes(10);

    uuid_bytes = set_byte(
        uuid_bytes, 6,
        (b'0111' || get_byte(uuid_bytes, 6)::bit(4))::bit(8)::int
    );

    uuid_bytes = set_byte(
        uuid_bytes, 8,
        (b'10' || get_byte(uuid_bytes, 8)::bit(6))::bit(8)::int
    );

    RETURN encode(uuid_bytes, 'hex')::uuid;
END;
$$ LANGUAGE plpgsql VOLATILE;

INSERT INTO data_sources (id, source_name, file_name, source_type, description) VALUES
(uuid_generate_v7(), 'Vísent CDRView - Antennas', 'antenas_flp.csv', 'SYNTHETIC_DATASET', 'Antenas, coordenadas, municipio y cluster.'),
(uuid_generate_v7(), 'Vísent CDRView - Concentration', 'tensor_concentracao.csv', 'SYNTHETIC_DATASET', 'Concentración territorial y mapa de calor.'),
(uuid_generate_v7(), 'Vísent CDRView - Road Flows', 'tensor_fluxo_vias.csv', 'SYNTHETIC_DATASET', 'Corredores y flujos agregados.'),
(uuid_generate_v7(), 'Vísent CDRView - Common Trajectories', 'trajetos_comuns.csv', 'SYNTHETIC_DATASET', 'Trayectos k-anonimizados.'),
(uuid_generate_v7(), 'Vísent CDRView - OD Matrix', 'tensor_od.csv', 'SYNTHETIC_DATASET', 'Matriz origen-destino por cluster.'),
(uuid_generate_v7(), 'Vísent CDRView - Travel Time', 'tensor_tempo_deslocamento.csv', 'SYNTHETIC_DATASET', 'Tiempo o distancia media entre zonas.'),
(uuid_generate_v7(), 'Vísent CDRView - Subscribers', 'assinantes.csv', 'SYNTHETIC_DATASET', 'Segmentación demográfica sintética.'),
(uuid_generate_v7(), 'Vísent CDRView - Privacy Summary', 'sumario_kanon.csv', 'SYNTHETIC_DATASET', 'Evidencia de privacidad y anonimización.'),
(uuid_generate_v7(), 'Vísent CDRView - Mobility', 'tensor_mobilidade.csv', 'SYNTHETIC_DATASET', 'Base bruta de movilidad.'),
(uuid_generate_v7(), 'Vísent CDRView - Sequences', 'tensor_sequencias.csv', 'SYNTHETIC_DATASET', 'Secuencia de antenas por usuario/día.');
