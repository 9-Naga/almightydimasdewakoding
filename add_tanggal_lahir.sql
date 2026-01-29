-- Add tanggal_lahir column to customer_profile table
ALTER TABLE customer_profile ADD COLUMN tanggal_lahir DATE;

-- Update existing records with a default random date (e.g., between 1980-01-01 and 2000-12-31)
-- Since we want a "random" date, but standard SQL might vary, we'll set a static default for simplicity and consistency, 
-- or use a random function if the DB supports it (assuming PostgreSQL or MySQL).
-- Here is a safe approach updating all to a default date, which the user said "bebas" (free/any).

UPDATE customer_profile SET tanggal_lahir = '1990-01-01' WHERE tanggal_lahir IS NULL;
