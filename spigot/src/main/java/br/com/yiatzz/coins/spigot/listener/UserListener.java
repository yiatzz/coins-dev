package br.com.yiatzz.coins.spigot.listener;

import br.com.yiatzz.coins.spigot.CoinsSpigotApplication;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserListener implements Listener {

    private final Provider<CoinsSpigotApplication> applicationProvider;

    @Inject
    public UserListener(Provider<CoinsSpigotApplication> applicationProvider) {
        this.applicationProvider = applicationProvider;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        applicationProvider.get().handleUserLoadInfos(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        applicationProvider.get().handleUserUnload(event.getPlayer());
    }
}
