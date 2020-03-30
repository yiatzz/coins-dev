package br.com.yiatzz.coins.core.user;

import java.util.UUID;

public interface User {

    UUID getUUID();

    String getName();

    double getCoins();

    boolean has(double value);

    void withdraw(double value);

    void deposit(double value);

    void define(double value);
}
