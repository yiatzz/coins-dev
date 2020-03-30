package br.com.yiatzz.coins.core.config;

public class CoinsCoreConfig {

    private final String jdbcUrl;
    private final String databaseUser;
    private final String databasePassword;

    public CoinsCoreConfig(String jdbcUrl, String databaseUser, String databasePassword) {
        this.jdbcUrl = jdbcUrl;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }
}
