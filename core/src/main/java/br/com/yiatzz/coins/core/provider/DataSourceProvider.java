package br.com.yiatzz.coins.core.provider;

import br.com.yiatzz.coins.core.config.CoinsCoreConfig;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataSourceProvider implements Provider<DataSource> {

    private final HikariDataSource dataSource;

    @Inject
    public DataSourceProvider(CoinsCoreConfig coreConfig) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(coreConfig.getJdbcUrl());
        hikariConfig.setUsername(coreConfig.getDatabaseUser());
        hikariConfig.setPassword(coreConfig.getDatabasePassword());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(hikariConfig);

        createTables();
    }

    private void createTables() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatemente = connection.prepareStatement("create table if not exists coinsUsers (\n" +
                    "  id          int auto_increment primary key, \n" +
                    "  name        varchar(16)  not null,\n" +
                    "  coins       double not null DEFAULT 0.0," +
                    "  constraint  coinsUsers_uindex\n" +
                    "  unique (id),\n" +
                    "  constraint coinsUsers_name_uindex\n" +
                    "  unique (name)\n" +
                    ") engine = InnoDB;");

            preparedStatemente.executeUpdate();
            preparedStatemente.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DataSource get() {
        return dataSource;
    }
}
