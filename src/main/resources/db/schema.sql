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
account_id INTEGER NOT NULL,
category_id INTEGER NOT NULL,
amount NUMERIC NOT NULL CHECK (amount > 0),
date TEXT NOT NULL,
description TEXT,

-- Foreign keys entre las cuentas o las categorias y las transacciones.
FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE RESTRICT,
FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE RESTRICT
);

-- Índices básicos para rendimiento en filtros habituales
CREATE INDEX IF NOT EXISTS index_financeTransaction_date ON finance_transaction(date);
CREATE INDEX IF NOT EXISTS index_financeTransaction_account ON finance_transaction(account_id);
CREATE INDEX IF NOT EXISTS index_financeTransaction_category ON finance_transaction(category_id);

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