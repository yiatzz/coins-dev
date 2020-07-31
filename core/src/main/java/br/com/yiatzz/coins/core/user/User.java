package br.com.yiatzz.coins.core.user;

public interface User {

    String getName();

    double getCoins();

    boolean has(double value);

    void withdraw(double value);

    void deposit(double value);

    void define(double value);
}
