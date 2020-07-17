package br.com.yiatzz.coins.core.user;

import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public interface UserController {

    void getUser(String name, Consumer<User> userConsumer);

    User getUser(String name);

    double getUserCoins(String name);

    void createUser(UUID uuid, String name, double coins, Consumer<Boolean> consumer);

    void removeUser(UUID uuid, Consumer<Boolean> result);

    void getUser(UUID uuid, Consumer<User> userConsumer);

    void updateUserCoins(UUID uuid, double newValue, Consumer<Boolean> consumer);

    void getUsers(Consumer<Set<User>> consumer);

    void getRanking(Consumer<LinkedList<User>> consumer);
}
