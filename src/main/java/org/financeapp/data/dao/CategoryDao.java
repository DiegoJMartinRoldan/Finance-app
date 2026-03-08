package org.financeapp.data.dao;

import org.financeapp.data.db.Database;
import org.financeapp.domain.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    // Buscar todas las categorias
    public List<Category> findAll() throws Exception {
        String sql = "SELECT id, name, kind FROM category ORDER BY kind, name";
        List<Category> categories = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                categories.add(new Category(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("kind")
                ));
            }
        }
        return categories;
    }

    // Buscar categoría por ID
    public Category findById(int id) throws Exception {
        String sql = "SELECT id, name, kind FROM category WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Category(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("kind")
                    );
                }
            }
        }
        return null;
    }

    // Insertar categoría
    public int insert(Category category) throws Exception {
        String sql = "INSERT INTO category (name, kind) VALUES (?, ?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getKind());
            return preparedStatement.executeUpdate();
        }
    }

    // Actualizar categoría
    public int update(Category category) throws Exception {
        String sql = "UPDATE category SET name = ?, kind = ? WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getKind());
            preparedStatement.setInt(3, category.getId());
            return preparedStatement.executeUpdate();
        }
    }

    // Eliminar categoría.
    public int deleteById(int id) throws Exception {
        String sql = "DELETE FROM category WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate();
        }
    }

    // Opcional: útil para validaciones
    public boolean existsByName(String name) throws Exception {
        String sql = "SELECT 1 FROM category WHERE name = ? LIMIT 1";

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, name);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Utilidad para detectar el caso en que se quiera borrar una categoría con transacciónes aplicadas.
    public static boolean foreignKeyFail(SQLException exception) {

        String message = exception.getMessage();
        return message != null && message.toLowerCase().contains("Foreign Key failed");
    }
}
