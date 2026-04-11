package org.financeapp.data.dao;

import org.financeapp.data.db.Database;
import org.financeapp.domain.FinanceTransaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinanceTransactionDao {

    public List<FinanceTransaction> findAll() throws Exception {
        final String sql =
                """
                SELECT id, type, account_id, to_account_id, category_id, amount, date, description
                FROM finance_transaction
                ORDER BY date DESC, id DESC
                """;

        List<FinanceTransaction> list = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int toAccountValue = resultSet.getInt("to_account_id");
                Integer toAccountId = resultSet.wasNull() ? null : toAccountValue;

                int categoryValue = resultSet.getInt("category_id");
                Integer categoryId = resultSet.wasNull() ? null : categoryValue;

                String stringDate = resultSet.getString("date");
                LocalDate date = (stringDate == null || stringDate.isBlank()) ? null : LocalDate.parse(stringDate);

                list.add(new FinanceTransaction(
                        resultSet.getInt("id"),
                        resultSet.getString("type"),
                        resultSet.getInt("account_id"),
                        toAccountId,
                        categoryId,
                        resultSet.getDouble("amount"),
                        date,
                        resultSet.getString("description")
                ));
            }
        }
        return list;
    }

    public FinanceTransaction findById(int id) throws Exception {
        final String sql =
                """
                SELECT id, type, account_id, to_account_id, category_id, amount, date, description
                FROM finance_transaction
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int toAccountValue = resultSet.getInt("to_account_id");
                    Integer toAccountId = resultSet.wasNull() ? null : toAccountValue;

                    int categoryValue = resultSet.getInt("category_id");
                    Integer categoryId = resultSet.wasNull() ? null : categoryValue;

                    String stringDate = resultSet.getString("date");
                    LocalDate date = (stringDate == null || stringDate.isBlank()) ? null : LocalDate.parse(stringDate);

                    return new FinanceTransaction(
                            resultSet.getInt("id"),
                            resultSet.getString("type"),
                            resultSet.getInt("account_id"),
                            toAccountId,
                            categoryId,
                            resultSet.getDouble("amount"),
                            date,
                            resultSet.getString("description")
                    );
                }
            }
        }
        return null;
    }

    public int insert(FinanceTransaction transaction) throws Exception {
        final String sql =
                """
                INSERT INTO finance_transaction (type, account_id, to_account_id, category_id, amount, date, description)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, transaction.getType());
            preparedStatement.setInt(2, transaction.getAccountId());

            if (transaction.getToAccountId() != null) {
                preparedStatement.setInt(3, transaction.getToAccountId());
            } else {
                preparedStatement.setNull(3, Types.INTEGER);
            }

            if (transaction.getCategoryId() != null) {
                preparedStatement.setInt(4, transaction.getCategoryId());
            } else {
                preparedStatement.setNull(4, Types.INTEGER);
            }

            preparedStatement.setDouble(5, transaction.getAmount());
            preparedStatement.setString(6, transaction.getDate().toString());
            preparedStatement.setString(7, transaction.getDescription());

            return preparedStatement.executeUpdate();
        }
    }

    public int update(FinanceTransaction transaction) throws Exception {
        final String sql =
                """
                UPDATE finance_transaction
                SET type = ?, account_id = ?, to_account_id = ?, category_id = ?, amount = ?, date = ?, description = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, transaction.getType());
            preparedStatement.setInt(2, transaction.getAccountId());

            if (transaction.getToAccountId() != null) {
                preparedStatement.setInt(3, transaction.getToAccountId());
            } else {
                preparedStatement.setNull(3, Types.INTEGER);
            }

            if (transaction.getCategoryId() != null) {
                preparedStatement.setInt(4, transaction.getCategoryId());
            } else {
                preparedStatement.setNull(4, Types.INTEGER);
            }

            preparedStatement.setDouble(5, transaction.getAmount());
            preparedStatement.setString(6, transaction.getDate().toString());
            preparedStatement.setString(7, transaction.getDescription());
            preparedStatement.setInt(8, transaction.getId());

            return preparedStatement.executeUpdate();
        }
    }

    public int deleteById(int id) throws Exception {
        final String sql = "DELETE FROM finance_transaction WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate();
        }
    }

    public List<FinanceTransaction> findByType(String type) throws Exception {
        final String sql =
                """
                SELECT id, type, account_id, to_account_id, category_id, amount, date, description
                FROM finance_transaction
                WHERE type = ?
                ORDER BY date DESC, id DESC
                """;

        List<FinanceTransaction> list = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, type);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int toAccountValue = resultSet.getInt("to_account_id");
                    Integer toAccountId = resultSet.wasNull() ? null : toAccountValue;

                    int categoryValue = resultSet.getInt("category_id");
                    Integer categoryId = resultSet.wasNull() ? null : categoryValue;

                    String stringDate = resultSet.getString("date");
                    LocalDate date = (stringDate == null || stringDate.isBlank()) ? null : LocalDate.parse(stringDate);

                    list.add(new FinanceTransaction(
                            resultSet.getInt("id"),
                            resultSet.getString("type"),
                            resultSet.getInt("account_id"),
                            toAccountId,
                            categoryId,
                            resultSet.getDouble("amount"),
                            date,
                            resultSet.getString("description")
                    ));
                }
            }
        }

        return list;
    }

    public List<FinanceTransaction> findByAccountId(int accountId) throws Exception {
        final String sql =
                """
                SELECT id, type, account_id, to_account_id, category_id, amount, date, description
                FROM finance_transaction
                WHERE account_id = ? OR to_account_id = ?
                ORDER BY date DESC, id DESC
                """;

        List<FinanceTransaction> list = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, accountId);
            preparedStatement.setInt(2, accountId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int toAccountValue = resultSet.getInt("to_account_id");
                    Integer toAccountId = resultSet.wasNull() ? null : toAccountValue;

                    int categoryValue = resultSet.getInt("category_id");
                    Integer categoryId = resultSet.wasNull() ? null : categoryValue;

                    String stringDate = resultSet.getString("date");
                    LocalDate date = (stringDate == null || stringDate.isBlank()) ? null : LocalDate.parse(stringDate);

                    list.add(new FinanceTransaction(
                            resultSet.getInt("id"),
                            resultSet.getString("type"),
                            resultSet.getInt("account_id"),
                            toAccountId,
                            categoryId,
                            resultSet.getDouble("amount"),
                            date,
                            resultSet.getString("description")
                    ));
                }
            }
        }

        return list;
    }

    public List<FinanceTransaction> findByCategoryId(int categoryId) throws Exception {
        final String sql =
                """
                SELECT id, type, account_id, to_account_id, category_id, amount, date, description
                FROM finance_transaction
                WHERE category_id = ?
                ORDER BY date DESC, id DESC
                """;

        List<FinanceTransaction> list = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, categoryId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int toAccountValue = resultSet.getInt("to_account_id");
                    Integer toAccountId = resultSet.wasNull() ? null : toAccountValue;

                    int categoryValue = resultSet.getInt("category_id");
                    Integer currentCategoryId = resultSet.wasNull() ? null : categoryValue;

                    String stringDate = resultSet.getString("date");
                    LocalDate date = (stringDate == null || stringDate.isBlank()) ? null : LocalDate.parse(stringDate);

                    list.add(new FinanceTransaction(
                            resultSet.getInt("id"),
                            resultSet.getString("type"),
                            resultSet.getInt("account_id"),
                            toAccountId,
                            currentCategoryId,
                            resultSet.getDouble("amount"),
                            date,
                            resultSet.getString("description")
                    ));
                }
            }
        }

        return list;
    }

    public double totalByCategoryId(int categoryId) throws Exception {
        final String sql =
                """
                SELECT COALESCE(SUM(amount), 0)
                FROM finance_transaction
                WHERE category_id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, categoryId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble(1);
                }
            }
        }

        return 0;
    }

    public double totalIncomingTransfersByAccount(int accountId) throws Exception {
        final String sql =
                """
                SELECT COALESCE(SUM(amount), 0)
                FROM finance_transaction
                WHERE type = 'TRANSFER'
                  AND to_account_id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, accountId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble(1);
                }
            }
        }

        return 0;
    }

    public double totalOutgoingTransfersByAccount(int accountId) throws Exception {
        final String sql =
                """
                SELECT COALESCE(SUM(amount), 0)
                FROM finance_transaction
                WHERE type = 'TRANSFER'
                  AND account_id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, accountId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble(1);
                }
            }
        }

        return 0;
    }
}