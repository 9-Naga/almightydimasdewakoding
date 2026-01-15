-- Script untuk mengupdate produk plafond dari 3 produk lama ke 5 produk baru
-- Jalankan script ini di SQL Server Management Studio atau DBeaver

-- PENTING: Backup database dulu sebelum menjalankan script ini!

USE loan_db;
GO

-- Hapus semua loan application yang menggunakan produk lama
-- (Opsional - skip jika ingin mempertahankan data lama)
DELETE FROM loan_application;
GO

-- Hapus produk lama
DELETE FROM plafond;
GO

-- Insert 5 produk baru
-- Bronze: 1-5 juta, 12 bulan, 15%
INSERT INTO plafond (name, min_amount, max_amount, interest_rate, tenor_month, is_active)
VALUES ('Bronze', 1000000, 4999999, 15.00, 12, 1);

-- Silver: 5-20 juta, 24 bulan, 12%
INSERT INTO plafond (name, min_amount, max_amount, interest_rate, tenor_month, is_active)
VALUES ('Silver', 5000000, 19999999, 12.00, 24, 1);

-- Gold: 20-50 juta, 36 bulan, 10%
INSERT INTO plafond (name, min_amount, max_amount, interest_rate, tenor_month, is_active)
VALUES ('Gold', 20000000, 49999999, 10.00, 36, 1);

-- Platinum: 50-100 juta, 48 bulan, 8%
INSERT INTO plafond (name, min_amount, max_amount, interest_rate, tenor_month, is_active)
VALUES ('Platinum', 50000000, 99999999, 8.00, 48, 1);

-- Diamond: 100-500 juta, 60 bulan, 6%
INSERT INTO plafond (name, min_amount, max_amount, interest_rate, tenor_month, is_active)
VALUES ('Diamond', 100000000, 500000000, 6.00, 60, 1);

GO

-- Verifikasi hasil
SELECT * FROM plafond ORDER BY min_amount;
GO
