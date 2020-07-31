package br.com.yiatzz.coins.spigot;

import br.com.yiatzz.coins.spigot.module.CoinsSpigotModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.plugin.java.JavaPlugin;

public class CoinsSpigotPlugin extends JavaPlugin {

    private CoinsSpigotApplication coinsSpigotApplication;

    @Override
    public void onDisable() {
        coinsSpigotApplication.destroy();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Injector injector = Guice.createInjector(new CoinsSpigotModule(this));
        coinsSpigotApplication = injector.getInstance(CoinsSpigotApplication.class);
        coinsSpigotApplication.initialize();
    }

    public CoinsSpigotApplication getCoinsSpigotApplication() {
        return coinsSpigotApplication;
    }
}
