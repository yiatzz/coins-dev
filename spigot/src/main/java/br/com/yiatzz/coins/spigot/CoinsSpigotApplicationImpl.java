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
        User byName = coinsCache.select(player.getName());
        if (byName == null) {
            player.sendMessage("§cVocê não possui coins.");
            return;
        }

        player.sendMessage("§2Você possui §f" + numberFormat.format(byName.getCoins()) + " §2coins.");
    }

    @Override
    public void handleUserViewingInfoPerfomed(Player player, String target) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
        if (offlinePlayer == null) {
            return;
        }

        User byName = coinsCache.select(offlinePlayer.getName());
        if (byName == null) {
            userController.getUser(offlinePlayer.getName(), user -> {
                if (user == null) {
                    player.sendMessage("§cUsuário inválido.");
                    return;
                }

                player.sendMessage(target + " §2possui §f" + numberFormat.format(user.getCoins()) + " §2coins.");
            });

            return;
        }

        player.sendMessage(target + " §2possui §f" + numberFormat.format(byName.getCoins()) + " §2coins.");
    }

    @Override
    public void handleUserDepositPerfomed(Player player, double newValue) {
        User byName = coinsCache.select(player.getName());
        if (byName == null) {
            return;
        }

        double value = byName.getCoins() + newValue;

        byName.define(value);
        userController.updateUserCoins(player.getName(), value, aBoolean -> {
            if (!aBoolean) {
                player.sendMessage("§cAlgo de errado aconteceu.");
            }
        });
    }

    @Override
    public void handleOfflineUserDepositPerfomed(OfflinePlayer offlinePlayer, double newValue) {
        User byName = coinsCache.select(offlinePlayer.getName());
        if (byName == null) {
            userController.getUser(offlinePlayer.getName(), user -> {
                if (user == null) {
                    return;
                }

                double value = user.getCoins() + newValue;

                userController.updateUserCoins(offlinePlayer.getName(), value, aBoolean -> {
                    if (!aBoolean) {
                        System.out.println("Algo de errado aconteceu no pl de coins.");
                    }
                });
            });

            return;
        }

        double value = byName.getCoins() + newValue;
        userController.updateUserCoins(offlinePlayer.getName(), value, aBoolean -> {
            if (!aBoolean) {
                System.out.println("Algo de errado aconteceu no pl de coins.");
            }
        });
    }

    @Override
    public void handleUserCoinsWithdrawPerfomed(Player player, double newValue) {
        User byName = coinsCache.select(player.getName());
        if (byName == null) {
            player.sendMessage("§cAlgo de errado aconteceu.");
            return;
        }

        byName.withdraw(newValue);

        if (byName.getCoins() < 0) {
            byName.define(0);
        }

        userController.updateUserCoins(player.getName(), byName.getCoins(), aBoolean -> {
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

        User byName = coinsCache.select(player.getName());
        if (byName == null) {
            player.sendMessage("§cAlgo de errado aconteceu.");
            return;
        }

        if (!byName.has(value)) {
            player.sendMessage("§cVocê não possui essa quantia de coins.");
            return;
        }

        User targetByUniqueId = coinsCache.select(target.getName());
        if (targetByUniqueId == null) {
            player.sendMessage("§cUsuário inválido.");
            return;
        }

        byName.define(byName.getCoins() - value);
        targetByUniqueId.define(targetByUniqueId.getCoins() + value);

        userController.updateUserCoins(player.getName(), byName.getCoins(), aBoolean -> {
            if (!aBoolean) {
                player.sendMessage("§cAlgo de errado aconteceu.");
                return;
            }

            player.sendMessage("§eVocê enviou §f" + numberFormat.format(value) + " §ecoins para §f" + target.getName() + "§e.");
        });

        userController.updateUserCoins(target.getName(), targetByUniqueId.getCoins(), aBoolean -> {
            if (!aBoolean) {
                target.sendMessage("§cAlgo de errado aconteceu.");
                return;
            }

            target.sendMessage("§eVocê recebeu §f" + numberFormat.format(value) + " §ecoins de §f" + player.getName() + "§e.");
        });
    }

    @Override
    public void handleUserDefinedCoins(Player player, double newValue) {
        User byName = coinsCache.select(player.getName());
        if (byName == null) {
            player.sendMessage("§cAlgo de errado aconteceu.");
            return;
        }

        byName.define(newValue);

        userController.updateUserCoins(player.getName(), newValue, aBoolean -> {
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
        userController.getUser(player.getName(), user -> {
            if (player.hasMetadata("NPC")) {
                return;
            }

            if (user == null) {
                userController.createUser(player.getName(), 0.0, aBoolean -> {
                    if (!aBoolean) {
                        player.sendMessage("§cAlgo de errado aconteceu.");
                        return;
                    }

                    coinsCache.insert(new SimpleUser(player.getName(), 0.0));

                    player.sendMessage("§eEba! Agora você está em nosso banco de dados.");
                });
            } else {
                coinsCache.insert(user);
            }
        });
    }

    @Override
    public void handleUserUnload(Player player) {
        coinsCache.remove(player.getName());
    }

    @Override
    public UserController getUserController() {
        return userController;
    }

    @Override
    public double getUserCoins(String player) {
        User byName = coinsCache.select(player);

        if (byName == null) {
            return 0.0;
        }

        return byName.getCoins();
    }
}
