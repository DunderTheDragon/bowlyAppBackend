-- Rozszerzenie tabeli users o dane profilowe
ALTER TABLE users
ADD COLUMN gender VARCHAR(10),
ADD COLUMN age INT,
ADD COLUMN height_cm DOUBLE PRECISION,
ADD COLUMN weight_kg DOUBLE PRECISION,
ADD COLUMN target_weight_kg DOUBLE PRECISION,
ADD COLUMN weekly_change_rate_kg DOUBLE PRECISION,
ADD COLUMN activity_level DOUBLE PRECISION,
ADD COLUMN protein_ratio INT,
ADD COLUMN fat_ratio INT,
ADD COLUMN carbs_ratio INT,
ADD COLUMN is_dark_theme BOOLEAN,
ADD COLUMN show_batch_onboarding BOOLEAN;

-- Ustawiamy wartosci domyslne dopiero PO dodaniu, zeby nie psuc istniejacych rekordow
ALTER TABLE users ALTER COLUMN gender SET DEFAULT 'MALE';
ALTER TABLE users ALTER COLUMN age SET DEFAULT 25;
ALTER TABLE users ALTER COLUMN height_cm SET DEFAULT 180.0;
ALTER TABLE users ALTER COLUMN weight_kg SET DEFAULT 80.0;
ALTER TABLE users ALTER COLUMN target_weight_kg SET DEFAULT 75.0;
ALTER TABLE users ALTER COLUMN weekly_change_rate_kg SET DEFAULT 0.5;
ALTER TABLE users ALTER COLUMN activity_level SET DEFAULT 1.375;
ALTER TABLE users ALTER COLUMN protein_ratio SET DEFAULT 30;
ALTER TABLE users ALTER COLUMN fat_ratio SET DEFAULT 30;
ALTER TABLE users ALTER COLUMN carbs_ratio SET DEFAULT 40;
ALTER TABLE users ALTER COLUMN show_batch_onboarding SET DEFAULT TRUE;


-- Rozszerzenie tabeli dziennika (consumed_portions), by można było zjeść sam produkt, bez patelni
ALTER TABLE consumed_portions
ADD COLUMN product_id BIGINT REFERENCES products(id) ON DELETE SET NULL;
