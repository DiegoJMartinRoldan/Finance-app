package org.financeapp.data.dao;

import org.financeapp.data.db.Database;
import org.financeapp.domain.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDao {


    public List<Account> findAll() throws Exception {
        final String sql =
                """
                SELECT id, name, type, initial_balance
                FROM account
                ORDER BY id
                """;

        List<Account> accounts = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                accounts.add(new Account(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("type"),
                        resultSet.getDouble("initial_balance")
                ));
            }
        }

        return

    accounts;
    }
    public Account findById(int id) throws Exception {
        final String sql =
            """
            SELECT id, name, type, initial_balance
            FROM account
            WHERE id = ?
            """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Account(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("type"),
                            resultSet.getDouble("initial_balance")
                    );
                }
            }
        }

        return null;
    }

    public void create(Account account) throws Exception {
        final String sql =
                """
                INSERT INTO account
                (name, type, initial_balance)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, account.getName());
            preparedStatement.setString(2, account.getType());
            preparedStatement.setDouble(3,

            account.getInitialBalance());

            preparedStatement.executeUpdate();
        }
    }
    public boolean update(Account account) throws Exception {
        final String sql =
                """
                UPDATE account
                SET name = ?, type = ?, initial_balance = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, account.getName());
            preparedStatement.setString(2, account.getType());
            preparedStatement.setDouble(3, account.getInitialBalance());
            preparedStatement.setInt(4, account.getId());

            int rowsAffected = preparedStatement.executeUpdate();
            return
                        rowsAffected > 0;
        }
    }

    public boolean deleteById(int id) throws Exception {
        final String sql =
                """
                DELETE FROM account
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            int rowsdeleted = preparedStatement.executeUpdate();
            return rowsdeleted > 0;
        }
    }

    public double allAccountTransactions(int id) throws Exception {
        final String sql =
                """
                SELECT COALESCE(SUM(
                CASE
                WHEN c.kind = 'INCOME' THEN ft.amount
                WHEN c.kind = 'EXPENSE' THEN -ft.amount
                END
                ), 0)
                FROM finance_transaction ft
                JOIN category c ON ft.category_id = c.id
                WHERE ft.account_id = ?
                """;


        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble(1);
                }
            }
        }

        return 0;
    }

    public double currentBalance(int accountId) throws Exception {
        Account account = findById(accountId);

        if (account == null) {
            throw new IllegalArgumentException("No se encuentra ninguna cuenta con id: " + accountId);
        }

        return account.getInitialBalance() + allAccountTransactions(accountId);
    }
}