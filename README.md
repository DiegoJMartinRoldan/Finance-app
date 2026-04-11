# Finance App -  Diego J. Martín Roldán.

Prototipo de una app de gestión de finanzas.
Doble Grado de Desarrollo de Aplicaciones Multiplataforma y Desarrolllo de Aplicaciones Web. Proyecto final 2025-2026.

# Arrancar la aplicación:
```bash
mvn clean javafx:run
```


## Funcionalidades principales

La aplicación permite gestionar:

- Cuentas
- Categorías (ingresos y gastos)
- Transacciones financieras

## Tecnologías utilizadas

- Java 17
- JavaFX
- SQLite (JDBC)
- Maven

## Estructura del proyecto

El proyecto sigue una organización por capas sencilla:

- `domain` → entidades del modelo (Account, Category, Transaction)
- `dao` → acceso a base de datos
- `service` → lógica de negocio y validaciones
- `controllers` → controladores de JavaFX
- `resources` → archivos FXML y script SQL

## Control de versiones

El repositorio se organiza en dos ramas principales:

- `main` → versión estable
- `dev` → desarrollo y nuevas funcionalidades

## Enlaces

- GitHub personal: https://github.com/DiegoJMartinRoldan
- Repositorio del proyecto: https://github.com/DiegoJMartinRoldan/Finance-app