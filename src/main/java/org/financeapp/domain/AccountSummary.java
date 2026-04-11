package org.financeapp.domain;

public class AccountSummary {

    private final int accountId;
    private final String accountName;
    private final double currentBalance;
    private final double totalIncome;
    private final double totalExpense;
    private final double netTransfers;

    public AccountSummary(
            int accountId,
            String accountName,
            double currentBalance,
            double totalIncome,
            double totalExpense,
            double netTransfers
    ) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.currentBalance = currentBalance;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netTransfers = netTransfers;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getNetTransfers() {
        return netTransfers;
    }
}