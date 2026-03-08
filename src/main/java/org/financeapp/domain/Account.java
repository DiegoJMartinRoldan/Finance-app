package org.financeapp.domain;

public class Account {

    private int id;
    private String name;
    private String type;
    private double initialBalance;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public Account(){
        // constructor vacio por ahora.
    }

    public Account(int id, String name, String type, double initialBalance) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.initialBalance = initialBalance;
    }


    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", initialBalance=" + initialBalance +
                '}';
    }
}
