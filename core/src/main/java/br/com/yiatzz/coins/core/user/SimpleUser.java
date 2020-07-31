package br.com.yiatzz.coins.core.user;

public class SimpleUser implements User {

    private final String name;
    private double balance;

    public SimpleUser(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getCoins() {
        return balance;
    }

    @Override
    public boolean has(double value) {
        return balance >= value;
    }

    @Override
    public void withdraw(double value) {
        this.balance -= value;
    }

    @Override
    public void deposit(double value) {
        this.balance += value;
    }

    @Override
    public void define(double value) {
        this.balance = value;
    }
}
