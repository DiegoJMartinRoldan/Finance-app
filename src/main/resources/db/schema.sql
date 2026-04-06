-- Base de datos del proyecto.

-- Activacion de claves foráneas en SQLite.
PRAGMA foreign_keys = ON;

-- Tabla para cuentas
CREATE TABLE IF NOT EXISTS account (
id INTEGER PRIMARY KEY AUTOINCREMENT,
name TEXT NOT NULL UNIQUE,
type TEXT NOT NULL CHECK (type IN ('CASH', 'BANK', 'CARD')),
initial_balance REAL NOT NULL DEFAULT 0
);

-- Tabla para categorías
CREATE TABLE IF NOT EXISTS category (
 id INTEGER PRIMARY KEY AUTOINCREMENT,
name TEXT NOT NULL UNIQUE,
kind TEXT NOT NULL CHECK (kind IN ('INCOME', 'EXPENSE'))
);

-- Tabla para transacciones financieras
CREATE TABLE IF NOT EXISTS finance_transaction (
id INTEGER PRIMARY KEY AUTOINCREMENT,
type TEXT NOT NULL CHECK (type IN ('INCOME', 'EXPENSE', 'TRANSFER')),
account_id INTEGER NOT NULL,
-- En categorias solo:
to_account_id INTEGER,
category_id INTEGER,
amount NUMERIC NOT NULL CHECK (amount > 0),
date TEXT NOT NULL,
description TEXT,

-- Foreign keys entre las cuentas o las categorias y las transacciones.
FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE RESTRICT,
FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE RESTRICT,
FOREIGN KEY (to_account_id) REFERENCES account(id) ON DELETE RESTRICT,
    -- Reglas para el tipo de transacción, sea ingreso/gasto o transferencia.
    CHECK (
    (type IN ('INCOME', 'EXPENSE') AND category_id IS NOT NULL AND to_account_id IS NULL)
    OR
    (type = 'TRANSFER' AND category_id IS NULL AND to_account_id IS NOT NULL AND account_id <> to_account_id)
    )
);

-- Índices básicos para rendimiento en filtros habituales
CREATE INDEX IF NOT EXISTS index_financeTransaction_date ON finance_transaction(date);
CREATE INDEX IF NOT EXISTS index_financeTransaction_account ON finance_transaction(account_id);
CREATE INDEX IF NOT EXISTS idx_finance_transaction_to_account ON finance_transaction(to_account_id);
CREATE INDEX IF NOT EXISTS index_financeTransaction_category ON finance_transaction(category_id);
CREATE INDEX IF NOT EXISTS idx_finance_transaction_type ON finance_transaction(type);

-- Seed para cuenta por defecto.
INSERT INTO account (name, type)
SELECT 'Efectivo', 'CASH'
WHERE NOT EXISTS (SELECT 1 FROM account);

-- Seed para categorias imprescindibles, para evitar duplicados en el create del CRUD.

INSERT INTO category (name, kind)
SELECT 'Nómina', 'INCOME'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='Nómina');

INSERT INTO category (name, kind)
SELECT 'Comida', 'EXPENSE'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='Comida');

INSERT INTO category (name, kind)
SELECT 'Transporte', 'EXPENSE'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='Transporte');

INSERT INTO category (name, kind)
SELECT 'Ocio', 'EXPENSE'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name='Ocio');