-- Dodanie kolumny barcode do tabeli products
ALTER TABLE products ADD COLUMN barcode VARCHAR(255);

-- Upewnienie się, że jeżeli barcode jest podany to jest unikalny
ALTER TABLE products ADD CONSTRAINT uk_products_barcode UNIQUE (barcode);
