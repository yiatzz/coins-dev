package br.com.yiatzz.coins.spigot;

import br.com.yiatzz.coins.core.cache.CoinsCache;
import br.com.yiatzz.coins.core.user.SimpleUser;
import br.com.yiatzz.coins.core.user.User;
import br.com.yiatzz.coins.core.user.UserController;
import br.com.yiatzz.coins.spigot.command.CommandCoins;
import br.com.yiatzz.coins.spigot.hook.VaultHook;
import br.com.yiatzz.coins.spigot.listener.UserListener;
import br.com.yiatzz.coins.spigot.wrapped.WrappedEconomy;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.NumberFormat;

public class CoinsSpigotApplicationImpl implements CoinsSpigotApplication {

    private final JavaPlugin plugin;
    private final UserController userController;
    private final CoinsCache coinsCache;
    private final NumberFormat numberFormat;
    private final WrappedEconomy wrappedEconomy;

    private final UserListener userListener;
    private final CommandCoins commandCoins;

    @Inject
    public CoinsSpigotApplicationImpl(@Named("coinsPlugin") Plugin plugin, UserController userController, CoinsCache coinsCache, WrappedEconomy wrappedEconomy, UserListener userListener, CommandCoins commandCoins) {
        this.plugin = (JavaPlugin) plugin;
        this.userController = userController;
        this.coinsCache = coinsCache;
        this.wrappedEconomy = wrappedEconomy;
        this.numberFormat = NumberFormat.getInstance();
        this.userListener = userListener;
        this.commandCoins = commandCoins;
    }

    @Override
    public void initialize() {
        hookVault();
        plugin.getCommand("coins").setExecutor(commandCoins);
        plugin.getServer().getPluginManager().registerEvents(userListener, plugin);
    }

    @Override
    public void destroy() {
        HandlerList.unregisterAll(plugin);
        plugin.getServer().getScheduler().cancelTasks(plugin);
    }

    @Override
    public void hookVault() {
        plugin.getServer().getServicesManager().register(Economy.class, wrappedEconomy, plugin, ServicePriority.Highest);
        plugin.getLogger().info("Vault hookado.");
    }

    @Override
    public void handleUserInfoPerfomed(Player player) {
        User byUniqueId = coinsCache.getIfPresent(player.getUniqueId());
        if (byUniqueId == null) {
            player.sendMessage("§cVocê não possui coins.");
            return;
        }

        player.sendMessage("§2Você possui §f" + numberFormat.format(byUniqueId.getCoins()) + " §2coins.");
    }

    @Override
    public void handleUserViewingInfoPerfomed(Player player, String target) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
        if (offlinePlayer == null) {
            return;
        }

        User byUniqueId = coinsCache.getIfPresent(offlinePlayer.getUniqueId());
        if (byUniqueId == null) {
            userController.getUser(target, user -> {
                if (user == null) {
                    player.sendMessage("§cUsuário inválido.");
                    return;
                }

                player.sendMessage(target + " §2possui §f" + numberFormat.format(user.getCoins()) + " §2coins.");
            });
            return;
        }

        player.sendMessage(target + " §2possui §f" + numberFormat.format(byUniqueId.getCoins()) + " §2coins.");
    }

    @Override
    public void handleUserDepositPerfomed(Player player, double newValue) {
        User byUniqueId = coinsCache.getIfPresent(player.getUniqueId());
        if (byUniqueId == null) {
            return;
        }

        double value = byUniqueId.getCoins() + newValue;

        byUniqueId.define(value);
        userController.updateUserCoins(player.getUniqueId(), value, aBoolean -> {
            if (!aBoolean) {
                player.sendMessage("§cAlgo de errado aconteceu.");
            }
        });
    }

    @Override
    public void handleOfflineUserDepositPerfomed(OfflinePlayer offlinePlayer, double newValue) {
        userController.getUser(offlinePlayer.getUniqueId(), user -> {
            double value = user.getCoins() + newValue;
            userController.updateUserCoins(offlinePlayer.getUniqueId(), value, aBoolean -> {
                if (!aBoolean) {
                    System.out.println("Algo de errado aconteceu no pl de coins.");
                }
            });
        });
    }

    @Override
    public void handleUserCoinsWithdrawPerfomed(Player player, double newValue) {
        User byUniqueId = coinsCache.getIfPresent(player.getUniqueId());
        if (byUniqueId == null) {
            player.sendMessage("§cAlgo de errado aconteceu.");
            return;
        }

        byUniqueId.withdraw(newValue);

        if (byUniqueId.getCoins() < 0) {
            byUniqueId.define(0);
        }

        userController.updateUserCoins(player.getUniqueId(), byUniqueId.getCoins(), aBoolean -> {
            if (!aBoolean) {
                player.sendMessage("§cAlgo de errado aconteceu.");
            }
        });
    }

    @Override
    public void handleUserPaymentPerfomed(Player player, Player target, double value) {
        if (value <= 0.0 || value <= 0) {
            player.sendMessage("§cValor inválido.");
            return;
        }

        if (player.getName().equals(target.getName())) {
            player.sendMessage("§cVocê não enviar coins á si mesmo.");
            return;
        }

        User byUniqueId = coinsCache.getIfPresent(player.getUniqueId());
        if (byUniqueId == null) {
            player.sendMessage("§cAlgo de errado aconteceu.");
            return;
        }

        if (!byUniqueId.has(value)) {
            player.sendMessage("§cVocê não possui essa quantia de coins.");
            return;
        }

        User targetByUniqueId = coinsCache.getIfPresent(target.getUniqueId());
        if (targetByUniqueId == null) {
            player.sendMessage("§cUsuário inválido.");
            return;
        }

        byUniqueId.define(byUniqueId.getCoins() - value);
        targetByUniqueId.define(targetByUniqueId.getCoins() + value);

        userController.updateUserCoins(player.getUniqueId(), byUniqueId.getCoins(), aBoolean -> {
            if (!aBoolean) {
                player.sendMessage("§cAlgo de errado aconteceu.");
                return;
            }

            player.sendMessage("§eVocê enviou §f" + numberFormat.format(value) + " §ecoins para §f" + target.getName() + "§e.");
        });

        userController.updateUserCoins(target.getUniqueId(), targetByUniqueId.getCoins(), aBoolean -> {
            if (!aBoolean) {
                target.sendMessage("§cAlgo de errado aconteceu.");
                return;
            }

            target.sendMessage("§eVocê recebeu §f" + numberFormat.format(value) + " §ecoins de §f" + player.getName() + "§e.");
        });
    }

    @Override
    public void handleUserDefinedCoins(Player player, double newValue) {
        User byUniqueId = coinsCache.getIfPresent(player.getUniqueId());
        if (byUniqueId == null) {
            player.sendMessage("§cAlgo de errado aconteceu.");
            return;
        }

        byUniqueId.define(newValue);

        userController.updateUserCoins(player.getUniqueId(), newValue, aBoolean -> {
            if (!aBoolean) {
                player.sendMessage("§cAlgo de errado aconteceu.");
            }
        });
    }

    @Override
    public void handleRankInfoPerfomed(Player player) {
        userController.getRanking(users -> {
            if (users.isEmpty()) {
                player.sendMessage("§cRank vazio.");
                return;
            }

            int i = 0;

            player.sendMessage("");
            player.sendMessage("§2     Top 10 jogadores com mais coins");
            player.sendMessage("");

            for (User user : users) {
                String prefix = i == 0 ? "§2[Magnata] " : "";
                prefix += VaultHook.getPlayerPrefix(user.getName());

                player.sendMessage("§f  " + (i + 1) + "º " + prefix + user.getName() + ": §7" + NumberFormat.getInstance().format(user.getCoins()) + " coins.");
                i++;
            }

            player.sendMessage("");
        });
    }

    @Override
    public void handleUserLoadInfos(Player player) {
        userController.getUser(player.getUniqueId(), user -> {
            if (player.hasMetadata("NPC")) {
                return;
            }

            if (user == null) {
                userController.createUser(player.getUniqueId(), player.getName(), 0.0, aBoolean -> {
                    if (!aBoolean) {
                        player.sendMessage("§cAlgo de errado aconteceu.");
                        return;
                    }

                    coinsCache.insert(new SimpleUser(player.getUniqueId(), player.getName(), 0.0));

                    player.sendMessage("§eEba! Agora você está em nosso banco de dados.");
                });
            } else {
                coinsCache.insert(user);
            }
        });
    }

    @Override
    public void handleUserUnload(Player player) {
        coinsCache.remove(player.getUniqueId());
    }

    @Override
    public UserController getUserController() {
        return userController;
    }

    @Override
    public double getUserCoins(String player) {
        User byUniqueId = coinsCache.find(player);

        if (byUniqueId == null) {
            return userController.getUserCoins(player);
        }

        return byUniqueId.getCoins();
    }
}
