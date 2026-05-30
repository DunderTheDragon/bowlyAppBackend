-- Tabela users: przechowuje dane logowania wszystkich domowników
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela products: Baza produktów własnych oraz pobranych z API
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    source VARCHAR(50) NOT NULL, -- np. 'LOCAL', 'SPOONACULAR', 'OPEN_FOOD_FACTS'
    external_id VARCHAR(255), -- ID z zewnętrznego API, jeśli istnieje
    kcal_per_100g DOUBLE PRECISION NOT NULL,
    protein_per_100g DOUBLE PRECISION NOT NULL,
    fat_per_100g DOUBLE PRECISION NOT NULL,
    carbs_per_100g DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_source_external_id UNIQUE (source, external_id)
);

-- Tabela meal_recipes: Przechowuje zdefiniowane przepisy
CREATE TABLE meal_recipes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    tags VARCHAR(255), -- np. 'kurczak, ryż, bez_glutenu'
    source VARCHAR(50) NOT NULL, -- np. 'LOCAL', 'SPOONACULAR'
    external_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela recipe_ingredients: Powiązanie konkretnych składników (produktów) z przepisem na dany posiłek i ich waga
CREATE TABLE recipe_ingredients (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL REFERENCES meal_recipes(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    weight_g DOUBLE PRECISION NOT NULL
);

-- Tabela batch_meals: Główne 'Patelnie' lub 'Garnki' - zrobione obiady
CREATE TABLE batch_meals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    recipe_id BIGINT REFERENCES meal_recipes(id) ON DELETE SET NULL, -- Można zrobić z przepisu lub z ręki
    is_depleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela batch_meal_segments: Podział patelni na różne elementy (np. segment Ryż, segment Kurczak z sosem)
CREATE TABLE batch_meal_segments (
    id BIGSERIAL PRIMARY KEY,
    batch_meal_id BIGINT NOT NULL REFERENCES batch_meals(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    product_id BIGINT REFERENCES products(id) ON DELETE RESTRICT, -- Opcjonalne przypięcie czystego produktu do segmentu
    initial_weight_g DOUBLE PRECISION NOT NULL,
    current_weight_g DOUBLE PRECISION NOT NULL,
    -- Zsumowane makro na cały segment
    total_kcal DOUBLE PRECISION NOT NULL,
    total_protein DOUBLE PRECISION NOT NULL,
    total_fat DOUBLE PRECISION NOT NULL,
    total_carbs DOUBLE PRECISION NOT NULL,
    -- Zabezpieczenie przed ujemną wagą
    CONSTRAINT chk_current_weight CHECK (current_weight_g >= 0)
);

-- Tabela consumed_portions: Dziennik zjedzonych porcji z rozbiciem na poszczególne segmenty
CREATE TABLE consumed_portions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    segment_id BIGINT REFERENCES batch_meal_segments(id) ON DELETE SET NULL,
    consumed_weight_g DOUBLE PRECISION NOT NULL,
    meal_date DATE NOT NULL, -- Konkretny dzień np. 2023-10-25
    meal_type VARCHAR(50) NOT NULL, -- np. 'BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
