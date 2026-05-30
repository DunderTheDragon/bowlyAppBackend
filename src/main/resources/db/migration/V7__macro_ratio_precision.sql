-- Dokładniejsze proporcje makro (suma zawsze 100.0 w wartościach rzeczywistych)
ALTER TABLE users
    ALTER COLUMN protein_ratio TYPE DOUBLE PRECISION USING protein_ratio::double precision,
    ALTER COLUMN fat_ratio TYPE DOUBLE PRECISION USING fat_ratio::double precision,
    ALTER COLUMN carbs_ratio TYPE DOUBLE PRECISION USING carbs_ratio::double precision;

ALTER TABLE users ALTER COLUMN protein_ratio SET DEFAULT 30.0;
ALTER TABLE users ALTER COLUMN fat_ratio SET DEFAULT 30.0;
ALTER TABLE users ALTER COLUMN carbs_ratio SET DEFAULT 40.0;
