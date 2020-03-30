package br.com.yiatzz.coins.spigot;

import br.com.yiatzz.coins.core.user.User;
import br.com.yiatzz.coins.core.user.UserController;
import br.com.yiatzz.coins.spigot.command.CommandCoins;
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
    private final NumberFormat numberFormat;
    private final WrappedEconomy wrappedEconomy;

    private final UserListener userListener;
    private final CommandCoins commandCoins;

    private final boolean converterBoolean;

    @Inject
    public CoinsSpigotApplicationImpl(@Named("coinsPlugin") Plugin plugin, UserController userController, WrappedEconomy wrappedEconomy, UserListener userListener, CommandCoins commandCoins, @Named("converter") boolean converterBoolean) {
        this.plugin = (JavaPlugin) plugin;
        this.userController = userController;
        this.wrappedEconomy = wrappedEconomy;
        this.numberFormat = NumberFormat.getInstance();
        this.userListener = userListener;
        this.commandCoins = commandCoins;
        this.converterBoolean = converterBoolean;
    }

    @Override
    public void initialize() {
        if (converterBoolean) {
            convert();
        }

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
        if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
            plugin.getServer().getServicesManager().register(Economy.class, wrappedEconomy, plugin, ServicePriority.Highest);
            plugin.getLogger().info("Vault hookado.");
        }
    }

    @Override
    public void convert() {
        userController.getUsersToConvert(users -> users.forEach((name, coins) -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            if (offlinePlayer == null) {
                plugin.getLogger().warning("Tentei converter o " + name + " mas ele é nulo :shrug:");
                return;
            }

            userController.createUser(offlinePlayer.getUniqueId(), name, coins, aBoolean -> {
                if (!aBoolean) {
                    plugin.getLogger().warning("Não deu pra converter o " + name + ".");
                    return;
                }

                plugin.getLogger().info(name + " convertido com sucesso! Agora ele também usa pl bom.");
            });
        }));
    }

    @Override
    public void handleUserInfoPerfomed(Player player) {
        userController.getUser(player.getUniqueId(), user -> {
            if (user == null) {
                player.sendMessage("§2Você possui §f0 §2coins.");
            } else {
                player.sendMessage("§2Você possui §f" + numberFormat.format(user.getCoins()) + " §2coins.");
            }
        });
    }

    @Override
    public void handleUserViewingInfoPerfomed(Player player, Player target) {
        userController.getUser(target.getUniqueId(), user -> {
            if (user == null) {
                player.sendMessage("§cUsuário inválido.");
            } else {
                player.sendMessage("§f" + user.getName() + " §2possui: §f" + numberFormat.format(user.getCoins()) + " §2coins.");
            }
        });
    }

    @Override
    public void handleUserDepositPerfomed(Player player, double newValue) {
        userController.updateUserCoins(player.getUniqueId(), newValue, aBoolean -> {
            if (!aBoolean) {
                player.sendMessage("§cAlgo de errado aconteceu.");
                return;
            }

            player.sendMessage("§eForam adicionados §f" + numberFormat.format(newValue) + " §ecoins á você.");
        });
    }

    @Override
    public void handleUserCoinsWithdrawPerfomed(Player player, double newValue) {
        userController.getUser(player.getUniqueId(), user -> userController.updateUserCoins(player.getUniqueId(), user.getCoins() - newValue, aBoolean -> {
            if (!aBoolean) {
                player.sendMessage("§cAlgo de errado aconteceu.");
                return;
            }

            player.sendMessage("§cForam removidos §f" + numberFormat.format(newValue) + " §ccoins de você.");
        }));
    }

    @Override
    public void handleUserPaymentPerfomed(Player player, Player target, double value) {
        if (player.getName().equals(target.getName())) {
            player.sendMessage("§cVocê não enviar coins á si mesmo.");
            return;
        }

        userController.getUser(player.getUniqueId(), user -> userController.getUser(target.getUniqueId(), targetUser -> {
            if (!user.has(value)) {
                player.sendMessage("§cVocê não possui essa quantia de coins.");
                return;
            }

            userController.updateUserCoins(player.getUniqueId(), user.getCoins() - value, aBoolean -> {
                if (!aBoolean) {
                    player.sendMessage("§cAlgo de errado aconteceu.");
                    return;
                }

                player.sendMessage("§eVocê enviou §f" + numberFormat.format(value) + " §ecoins para §f" + target.getName() + "§e.");
            });

            userController.updateUserCoins(target.getUniqueId(), targetUser.getCoins() + value, aBoolean -> {
                if (!aBoolean) {
                    target.sendMessage("§cAlgo de errado aconteceu.");
                    return;
                }

                target.sendMessage("§eVocê recebeu §f" + numberFormat.format(value) + " §ecoins de §f" + player.getName() + "§e.");
            });
        }));
    }

    @Override
    public void handleUserDefinedCoins(Player player, double newValue) {
        userController.updateUserCoins(player.getUniqueId(), newValue, aBoolean -> {
            if (!aBoolean) {
                player.sendMessage("§cAlgo de errado aconteceu.");
                return;
            }

            player.sendMessage("§eSeus coins foram definidos para §f" + numberFormat.format(newValue) + "§e.");
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

                player.sendMessage("§f  " + (i + 1) + "º " + prefix + user.getName() + ": §7" + NumberFormat.getInstance().format(user.getCoins()) + " coins.");
                i++;
            }

            player.sendMessage("");
        });
    }

    @Override
    public void handleUserLoadInfos(Player player) {
        userController.getUser(player.getUniqueId(), user -> {
            if (user == null) {
                userController.createUser(player.getUniqueId(), player.getName(), 0.0, aBoolean -> {
                    if (!aBoolean) {
                        player.sendMessage("§cAlgo de errado aconteceu.");
                        return;
                    }

                    player.sendMessage("§eEba! Agora você está em nosso banco de dados.");
                });
            }
        });
    }

    @Override
    public double getUserCoins(Player player) {
        return userController.getUserCoins(player.getName());
    }

    @Override
    public double getUserCoins(String player) {
        return userController.getUserCoins(player);
    }
}
