package br.com.yiatzz.coins.core.module;

import br.com.yiatzz.coins.core.config.CoinsCoreConfig;
import br.com.yiatzz.coins.core.provider.DataSourceProvider;
import br.com.yiatzz.coins.core.user.UserController;
import br.com.yiatzz.coins.core.user.UserControllerImpl;
import com.google.inject.AbstractModule;

import javax.sql.DataSource;

public class CoinsCoreModule extends AbstractModule {

    private final CoinsCoreConfig coinsCoreConfig;

    public CoinsCoreModule(CoinsCoreConfig coinsCoreConfig) {
        this.coinsCoreConfig = coinsCoreConfig;
    }

    @Override
    protected void configure() {
        bind(CoinsCoreConfig.class).toInstance(coinsCoreConfig);
        bind(DataSource.class).toProvider(DataSourceProvider.class).asEagerSingleton();
        bind(UserController.class).to(UserControllerImpl.class).asEagerSingleton();
    }
}
