# Finance App -  Diego J. Martín Roldán.

Prototipo de una app de gestión de finanzas.
Doble Grado de Desarrollo de Aplicaciones Multiplataforma y Desarrolllo de Aplicaciones Web. Proyecto final 2025-2026.

# Arrancar la aplicación:
```bash
mvn clean javafx:run
```

La aplicación permite gestionar:
- Cuentas
- Categorías (ingresos y gastos)
- Transacciones financieras

## Tecnologías utilizadas
- Java 17
- JavaFX
- SQLite(JDBC)
- Maven

## Estructura del proyecto

El proyecto está organizado de forma sencilla siguiendo varias capas:

- domain      -- entidades del modelo (Account, Category, Transaction)
- dao         -- acceso a base de datos
- services    -- lógica de negocio
- controllers -- controladores de JavaFX
- resources   -- fxml y base de datos .sql

## Ramas del repositorio
- Main: Rama principal
- dev: Rama de desarrollo

## Enlaces de interés
- https://github.com/DiegoJMartinRoldan
- https://github.com/DiegoJMartinRoldan/Finance-app