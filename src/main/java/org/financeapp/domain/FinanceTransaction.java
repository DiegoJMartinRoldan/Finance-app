package org.financeapp.domain;

import java.time.LocalDate;

public class FinanceTransaction {


    private int id;
    private String type;
    private int accountId;
    private Integer toAccountId;
    private Integer categoryId;
    private double amount;
    private LocalDate date;
    private String description;

    public FinanceTransaction() {
    }

    public FinanceTransaction(int id, String type, int accountId, Integer toAccountId, Integer categoryId, double amount, LocalDate date, String description) {
        this.id = id;
        this.type = type;
        this.accountId = accountId;
        this.toAccountId = toAccountId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public Integer getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Integer toAccountId) {
        this.toAccountId = toAccountId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "FinanceTransaction{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", accountId=" + accountId +
                ", toAccountId=" + toAccountId +
                ", categoryId=" + categoryId +
                ", amount=" + amount +
                ", date=" + date +
                ", description='" + description + '\'' +
                '}';
    }
}
