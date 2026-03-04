# Arquitectura de la base de datos.

La aplicación utiliza una base de datos relacional SQLite.

## Modelo Entidad–Relación

El modelo se compone de tres entidades principales:

- Account
- Category
- FinanceTransaction

### Account
Cuenta financiera desde la cual se registran movimientos.

Campos:
- id (PK)
- name
- type

### Category
Permite clasificar las transacciones.

Campos:
- id (PK)
- name
- kind

### FinanceTransaction
Representa cada movimiento financiero registrado en la aplicación.

Campos:
- id (PK)
- account_id (FK)
- category_id (FK)
- amount
- date
- description

### Relaciones

- Una cuenta (Account) puede tener muchas transacciones (FinanceTransaction)
- Una categoría (Category) puede tener muchas transacciones (FinanceTransaction)
- Cada transferencia pertenece a una cuenta y una categoría en concreto.