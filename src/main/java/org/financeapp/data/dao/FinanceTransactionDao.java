package org.financeapp.data.dao;

import org.financeapp.data.db.Database;
import org.financeapp.domain.FinanceTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinanceTransactionDao {

    public List<FinanceTransaction> findAll() throws Exception {
        final String sql =
                """
                SELECT id, account_id, category_id, amount, date, description
                FROM finance_transaction
                ORDER BY date DESC, id DESC
                """;

        List<FinanceTransaction> list = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                list.add(new FinanceTransaction(
                        resultSet.getInt("id"),
                        resultSet.getInt("account_id"),
                        resultSet.getInt("category_id"),
                        resultSet.getDouble("amount"),
                        LocalDate.parse(resultSet.getString("date")),
                        resultSet.getString("description")
                ));
            }
        }
        return list;
    }

    public FinanceTransaction findById(int id) throws Exception {
        final String sql =
                """
                SELECT id, account_id, category_id, amount, date, description
                FROM finance_transaction
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery())
                {
               if (resultSet.next()) {
                   String stringDate =  resultSet.getString("date");

                   LocalDate date = (stringDate == null || stringDate.isBlank()) ? null : LocalDate.parse(stringDate);

                   return new FinanceTransaction(
                           resultSet.getInt("id"),
                           resultSet.getInt("account_id"),
                           resultSet.getInt("category_id"),
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
                INSERT INTO finance_transaction (account_id, category_id, amount, date, description)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, transaction.getAccountId());
            preparedStatement.setInt(2, transaction.getCategoryId());
            preparedStatement.setDouble(3, transaction.getAmount());
            preparedStatement.setString(4, transaction.getDate().toString()); // ISO-8601
            preparedStatement.setString(5, transaction.getDescription());
            return preparedStatement.executeUpdate();
        }
    }

    public int update(FinanceTransaction transaction) throws Exception {
        final String sql =
                """
                UPDATE finance_transaction
                SET account_id = ?, category_id = ?, amount = ?, date = ?, description = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, transaction.getAccountId());
            preparedStatement.setInt(2, transaction.getCategoryId());
            preparedStatement.setDouble(3, transaction.getAmount());
            preparedStatement.setString(4, transaction.getDate().toString());
            preparedStatement.setString(5, transaction.getDescription());
            preparedStatement.setInt(6, transaction.getId());
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


}