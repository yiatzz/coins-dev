package br.com.yiatzz.coins.spigot.module;

import br.com.yiatzz.coins.core.cache.CoinsCache;
import br.com.yiatzz.coins.core.config.CoinsCoreConfig;
import br.com.yiatzz.coins.core.module.CoinsCoreModule;
import br.com.yiatzz.coins.spigot.CoinsSpigotApplication;
import br.com.yiatzz.coins.spigot.CoinsSpigotApplicationImpl;
import br.com.yiatzz.coins.spigot.CoinsSpigotPlugin;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class CoinsSpigotModule extends AbstractModule {

    private final CoinsSpigotPlugin coinsSpigotPlugin;
    private final CoinsCoreConfig coinsCoreConfig;
    private final CoinsCache coinsCache;

    public CoinsSpigotModule(CoinsSpigotPlugin coinsSpigotPlugin) {
        this.coinsSpigotPlugin = coinsSpigotPlugin;

        FileConfiguration config = coinsSpigotPlugin.getConfig();
        coinsCoreConfig = new CoinsCoreConfig(config.getString("database.url"), config.getString("database.user"),
                config.getString("database.password"));

        coinsCache = new CoinsCache();
    }

    @Override
    protected void configure() {
        install(new CoinsCoreModule(coinsCoreConfig, coinsCache));

        bind(Plugin.class).annotatedWith(Names.named("coinsPlugin")).toInstance(coinsSpigotPlugin);

        bind(CoinsSpigotApplication.class).to(CoinsSpigotApplicationImpl.class).asEagerSingleton();

        bindConstant().annotatedWith(Names.named("converter")).to(coinsSpigotPlugin.getConfig().getBoolean("converter"));
    }
}
