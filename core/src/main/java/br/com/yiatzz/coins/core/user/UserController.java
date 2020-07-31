package br.com.yiatzz.coins.core.user;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface UserController {

    void getUser(String name, Consumer<User> userConsumer);

    User getUser(String name);

    double getUserCoins(String name);

    void createUser(String name, double coins, Consumer<Boolean> consumer);

    void removeUser(String name, Consumer<Boolean> result);

    void updateUserCoins(String name, double newValue, Consumer<Boolean> consumer);

    void getUsers(Consumer<Set<User>> consumer);

    void getRanking(Consumer<List<User>> consumer);
}
