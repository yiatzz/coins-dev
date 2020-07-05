package br.com.yiatzz.coins.core.user;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class UserControllerImpl implements UserController {

    private static final String QUERY_GET_USERS = "SELECT `uniqueId`, `name`, `coins` FROM coinsUsers";
    private static final String QUERY_GET_COINS = "SELECT `coins` FROM coinsUsers WHERE coinsUsers.`name` = ?";
    private static final String QUERY_GET_USER_BY_NAME = "SELECT `uniqueId`, `coins` FROM coinsUsers WHERE coinsUsers.`name` = ?";
    private static final String QUERY_GET_USER_BY_UUID = "SELECT `name`, `coins` FROM coinsUsers WHERE coinsUsers.`uniqueId` = ?";
    private static final String QUERY_CREATE_USER = "INSERT INTO coinsUsers (`uniqueId`, `name`, `coins`) VALUES (?, ?, ?)";
    private static final String QUERY_DELETE_USER_BY_UUID = "DELETE FROM coinsUsers WHERE `uniqueId` = ?";
    private static final String QUERY_UPDATE_USER_COINS = "UPDATE coinsUsers SET coins = ? WHERE `uniqueId` = ?";
    private static final String QUERY_GET_RANKING = "SELECT `uniqueId`, `name`, `coins` FROM coinsUsers ORDER BY `coins` DESC LIMIT 10";

    private final ExecutorService executorService = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    private final DataSource dataSource;

    @Inject
    public UserControllerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void getUser(String name, Consumer<User> userConsumer) {
        executorService.submit(() -> {
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(QUERY_GET_USER_BY_NAME);
                preparedStatement.setString(1, name);

                ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.next()) {
                    userConsumer.accept(null);
                    resultSet.close();
                    return;
                }

                User user = new SimpleUser(UUID.fromString(resultSet.getString("uniqueId")), name, resultSet.getDouble("coins"));
                resultSet.close();
                userConsumer.accept(user);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public User getUser(String name) {
        CompletableFuture<User> future = new CompletableFuture<>();

        executorService.submit(() -> {
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(QUERY_GET_USER_BY_NAME);
                preparedStatement.setString(1, name);

                ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.next()) {
                    future.complete(null);
                    resultSet.close();
                    return;
                }

                User user = new SimpleUser(UUID.fromString(resultSet.getString("uniqueId")), name,
                        resultSet.getDouble("coins"));

                resultSet.close();
                future.complete(user);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return future.join();
    }

    @Override
    public double getUserCoins(String name) {
        CompletableFuture<Double> future = new CompletableFuture<>();

        executorService.submit(() -> {
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(QUERY_GET_COINS);
                preparedStatement.setString(1, name);

                ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.next()) {
                    future.complete(0.0);
                    resultSet.close();
                    return;
                }

                double value = resultSet.getDouble("coins");
                resultSet.close();
                future.complete(value);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return future.join();
    }

    @Override
    public void createUser(UUID uuid, String name, double coins, Consumer<Boolean> consumer) {
        executorService.submit(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(QUERY_CREATE_USER)) {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, name);
                preparedStatement.setDouble(3, coins);

                int result = preparedStatement.executeUpdate();
                consumer.accept(result > 0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void removeUser(UUID uuid, Consumer<Boolean> result) {
        executorService.submit(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(QUERY_DELETE_USER_BY_UUID)) {
                preparedStatement.setString(1, uuid.toString());

                int changedRows = preparedStatement.executeUpdate();

                result.accept(changedRows > 0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void getUser(UUID uuid, Consumer<User> userConsumer) {
        executorService.submit(() -> {
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(QUERY_GET_USER_BY_UUID);
                preparedStatement.setString(1, uuid.toString());

                ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.next()) {
                    userConsumer.accept(null);
                    resultSet.close();
                    return;
                }

                User user = new SimpleUser(uuid, resultSet.getString("name"), resultSet.getDouble("coins"));
                resultSet.close();
                userConsumer.accept(user);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void updateUserCoins(UUID uuid, double newValue, Consumer<Boolean> consumer) {
        executorService.submit(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(QUERY_UPDATE_USER_COINS)) {
                preparedStatement.setDouble(1, newValue);
                preparedStatement.setString(2, uuid.toString());

                int rows = preparedStatement.executeUpdate();
                consumer.accept(rows > 0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void getUsers(Consumer<Set<User>> consumer) {
        executorService.submit(() -> {
            Set<User> users = Sets.newHashSet();

            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(QUERY_GET_USERS)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    User user = new SimpleUser(UUID.fromString(resultSet.getString("uniqueId")), resultSet.getString("name"), resultSet.getDouble("coins"));
                    users.add(user);
                }

                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            consumer.accept(users);
        });
    }

    @Override
    public void getRanking(Consumer<LinkedHashSet<User>> consumer) {
        executorService.submit(() -> {
            LinkedHashSet<User> ranking = Sets.newLinkedHashSet();

            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(QUERY_GET_RANKING)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    User user = new SimpleUser(UUID.fromString(resultSet.getString("uniqueId")), resultSet.getString("name"), resultSet.getDouble("coins"));
                    ranking.add(user);
                }

                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            consumer.accept(ranking);
        });
    }
}
