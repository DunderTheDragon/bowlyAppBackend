-- Dodanie roli do tabeli uzytkownikow (domyslnie USER)
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Tabela przechowujaca globalne ustawienia aplikacji (klucze API)
CREATE TABLE app_settings (
    key_name VARCHAR(100) PRIMARY KEY,
    key_value TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
