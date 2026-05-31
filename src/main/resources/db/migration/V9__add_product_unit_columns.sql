-- Jednostki porcji produktu (np. sztuka, plasterek)
ALTER TABLE products ADD COLUMN unit_name VARCHAR(50);
ALTER TABLE products ADD COLUMN unit_weight_g DOUBLE PRECISION;
