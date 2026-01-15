-- SQL Script untuk Update Plafond di DBeaver
-- Jalankan script ini langsung di DBeaver SQL Editor

-- STEP 1: Hapus loan applications yang ada (opsional, skip jika ingin keep data lama)
-- DELETE FROM loan_application;

-- STEP 2: Update 3 produk existing
-- Update ID 1 (Silver lama) menjadi Bronze
UPDATE plafond 
SET name = 'Bronze',
    min_amount = 1000000,
    max_amount = 4999999,
    interest_rate = 15.00,
    tenor_month = 12,
    is_active = 1
WHERE id = 1;

-- Update ID 2 (Gold lama) menjadi Silver baru
UPDATE plafond 
SET name = 'Silver',
    min_amount = 5000000,
    max_amount = 19999999,
    interest_rate = 12.00,
    tenor_month = 24,
    is_active = 1
WHERE id = 2;

-- Update ID 3 (Platinum lama) menjadi Gold baru
UPDATE plafond 
SET name = 'Gold',
    min_amount = 20000000,
    max_amount = 49999999,
    interest_rate = 10.00,
    tenor_month = 36,
    is_active = 1
WHERE id = 3;

-- STEP 3: Insert 2 produk baru (Platinum dan Diamond)
-- Insert Platinum baru
INSERT INTO plafond (name, min_amount, max_amount, interest_rate, tenor_month, is_active)
VALUES ('Platinum', 50000000, 99999999, 8.00, 48, 1);

-- Insert Diamond baru
INSERT INTO plafond (name, min_amount, max_amount, interest_rate, tenor_month, is_active)
VALUES ('Diamond', 100000000, 500000000, 6.00, 60, 1);

-- STEP 4: Verifikasi hasil
SELECT id, name, min_amount, max_amount, interest_rate, tenor_month, is_active 
FROM plafond 
ORDER BY min_amount;

-- Expected result:
-- id=1: Bronze (1M-5M, 12mo, 15%)
-- id=2: Silver (5M-20M, 24mo, 12%)
-- id=3: Gold (20M-50M, 36mo, 10%)
-- id=4: Platinum (50M-100M, 48mo, 8%)
-- id=5: Diamond (100M-500M, 60mo, 6%)
