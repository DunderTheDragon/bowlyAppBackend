ALTER TABLE meal_recipes
    ADD COLUMN user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    ADD COLUMN is_single_meal BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE recipe_ingredients
    ADD COLUMN section_name VARCHAR(255) NOT NULL DEFAULT 'Główna część';
