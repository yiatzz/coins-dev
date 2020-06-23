package br.com.yiatzz.coins.spigot.hook;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    protected static Chat chat;

    static {
        setupChat();
    }

    public static String getPlayerPrefix(String name) {
        Player player = Bukkit.getPlayer(name);

        if (player != null) {
            return ChatColor.translateAlternateColorCodes('&', chat.getPlayerPrefix(player));
        } else {
            return ChatColor.translateAlternateColorCodes('&', chat.getPlayerPrefix("world", name));
        }
    }

    public static void setupChat() {
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return;
        }

        chat = rsp.getProvider();
    }
}