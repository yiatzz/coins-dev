package br.com.yiatzz.coins.core.user;

import java.util.UUID;

public class SimpleUser implements User {

    private final UUID uniqueId;
    private final String name;
    private double balance;

    public SimpleUser(UUID uniqueId, String name, double balance) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.balance = balance;
    }

    @Override
    public UUID getUUID() {
        return uniqueId;
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
