-- Snapshot wagi sekcji w momencie logowania porcji (ochrona historycznych wpisów dziennika)
ALTER TABLE consumed_portions
    ADD COLUMN weight_basis_g DOUBLE PRECISION;

UPDATE consumed_portions cp
SET weight_basis_g = bms.initial_weight_g
FROM batch_meal_segments bms
WHERE cp.segment_id = bms.id
  AND cp.weight_basis_g IS NULL;

-- Waga surowych składników przy tworzeniu sekcji (do wyświetlania przed/po gotowaniu)
ALTER TABLE batch_meal_segments
    ADD COLUMN raw_weight_g DOUBLE PRECISION;

UPDATE batch_meal_segments
SET raw_weight_g = initial_weight_g
WHERE raw_weight_g IS NULL;

-- Globalne naczynia instancji (tara)
CREATE TABLE weighing_containers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    weight_g DOUBLE PRECISION NOT NULL CHECK (weight_g > 0),
    image_base64 TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
