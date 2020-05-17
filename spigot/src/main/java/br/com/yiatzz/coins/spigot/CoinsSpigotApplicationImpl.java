package br.com.yiatzz.coins.spigot;

import br.com.yiatzz.coins.core.cache.CoinsCache;
import br.com.yiatzz.coins.core.user.SimpleUser;
import br.com.yiatzz.coins.core.user.User;
import br.com.yiatzz.coins.core.user.UserController;
import br.com.yiatzz.coins.spigot.command.CommandCoins;
import br.com.yiatzz.coins.spigot.listener.UserListener;
import br.com.yiatzz.coins.spigot.wrapped.WrappedEconomy;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.text.NumberFormat;
import java.util.Optional;

public class CoinsSpigotApplicationImpl implements CoinsSpigotApplication {

    private final JavaPlugin plugin;
    private final UserController userController;
    private final CoinsCache coinsCache;
    private final NumberFormat numberFormat;
    private final WrappedEconomy wrappedEconomy;

    private final UserListener userListener;
    private final CommandCoins commandCoins;

    private final boolean converterBoolean;

    @Inject
    public CoinsSpigotApplicationImpl(@Named("coinsPlugin") Plugin plugin, UserController userController, CoinsCache coinsCache, WrappedEconomy wrappedEconomy, UserListener userListener, CommandCoins commandCoins, @Named("converter") boolean converterBoolean) {
        this.plugin = (JavaPlugin) plugin;
        this.userController = userController;
        this.coinsCache = coinsCache;
        this.wrappedEconomy = wrappedEconomy;
        this.numberFormat = NumberFormat.getInstance();
        this.userListener = userListener;
        this.commandCoins = commandCoins;
        this.converterBoolean = converterBoolean;
    }

    @Override
    public void initialize() {
        hookVault();

        if (converterBoolean) {
            convert();
        }

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

                User user = new SimpleUser(offlinePlayer.getUniqueId(), name, coins);
                coinsCache.addElement(user);

                plugin.getLogger().info(name + " convertido com sucesso! Agora ele também usa pl bom.");
            });
        }));
    }

    @Override
    public void handleUserInfoPerfomed(Player player) {
        Optional<User> byUniqueId = coinsCache.getByUniqueId(player.getUniqueId());
        if (!byUniqueId.isPresent()) {
            player.sendMessage("§cVocê não possui coins.");
            return;
        }

        player.sendMessage("§2Você possui §f" + numberFormat.format(byUniqueId.get().getCoins()) + " §2coins.");
    }

    @Override
    public void handleUserViewingInfoPerfomed(Player player, String target) {
        Optional<User> byUniqueId = coinsCache.getByName(target);
        if (!byUniqueId.isPresent()) {
            userController.getUser(target, user -> {
                if (user == null) {
                    player.sendMessage("§cUsuário inválido.");
                    return;
                }

                player.sendMessage(target + " §2possui §f" + numberFormat.format(user.getCoins()) + " §2coins.");
            });
            return;
        }

        player.sendMessage(target + " §2possui §f" + numberFormat.format(byUniqueId.get().getCoins()) + " §2coins.");
    }

    @Override
    public void handleUserDepositPerfomed(Player player, double newValue) {
        Optional<User> byUniqueId = coinsCache.getByUniqueId(player.getUniqueId());
        if (!byUniqueId.isPresent()) {
            return;
        }

        User user = byUniqueId.get();
        double value = user.getCoins() + newValue;

        user.define(value);
        userController.updateUserCoins(player.getUniqueId(), value, aBoolean -> {
            if (!aBoolean) {
                player.sendMessage("§cAlgo de errado aconteceu.");
                return;
            }

            player.sendMessage("§eAgora você tem §f" + numberFormat.format(value) + "§e coins.");
        });
    }

    @Override
    public void handleOfflineUserDepositPerfomed(OfflinePlayer offlinePlayer, double newValue) {
        userController.getUser(offlinePlayer.getUniqueId(), user -> {
            double value = user.getCoins() + newValue;
            userController.updateUserCoins(offlinePlayer.getUniqueId(), value, aBoolean -> {
            });
        });
    }

    @Override
    public void handleUserCoinsWithdrawPerfomed(Player player, double newValue) {
        Optional<User> byUniqueId = coinsCache.getByUniqueId(player.getUniqueId());
        if (!byUniqueId.isPresent()) {
            player.sendMessage("§cAlgo de errado aconteceu.");
            return;
        }

        User user = byUniqueId.get();

        user.withdraw(newValue);

        if (user.getCoins() < 0) {
            user.define(0);
        }

        userController.updateUserCoins(player.getUniqueId(), user.getCoins(), aBoolean -> {
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

        Optional<User> byUniqueId = coinsCache.getByUniqueId(player.getUniqueId());
        if (!byUniqueId.isPresent()) {
            player.sendMessage("§cAlgo de errado aconteceu.");
            return;
        }

        User user = byUniqueId.get();
        if (!user.has(value)) {
            player.sendMessage("§cVocê não possui essa quantia de coins.");
            return;
        }

        Optional<User> targetByUniqueId = coinsCache.getByUniqueId(target.getUniqueId());
        if (!targetByUniqueId.isPresent()) {
            player.sendMessage("§cUsuário inválido.");
            return;
        }

        User targetUser = targetByUniqueId.get();

        user.define(user.getCoins() - value);
        targetUser.define(targetUser.getCoins() + value);

        userController.updateUserCoins(player.getUniqueId(), user.getCoins(), aBoolean -> {
            if (!aBoolean) {
                player.sendMessage("§cAlgo de errado aconteceu.");
                return;
            }

            player.sendMessage("§eVocê enviou §f" + numberFormat.format(value) + " §ecoins para §f" + target.getName() + "§e.");
        });

        userController.updateUserCoins(target.getUniqueId(), targetUser.getCoins(), aBoolean -> {
            if (!aBoolean) {
                target.sendMessage("§cAlgo de errado aconteceu.");
                return;
            }

            target.sendMessage("§eVocê recebeu §f" + numberFormat.format(value) + " §ecoins de §f" + player.getName() + "§e.");
        });
    }

    @Override
    public void handleUserDefinedCoins(Player player, double newValue) {
        Optional<User> byUniqueId = coinsCache.getByUniqueId(player.getUniqueId());
        if (!byUniqueId.isPresent()) {
            player.sendMessage("§cAlgo de errado aconteceu.");
            return;
        }

        byUniqueId.get().define(newValue);

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
                prefix += ChatColor.translateAlternateColorCodes('&', PermissionsEx.getPermissionManager().getUser(user.getUUID()).getPrefix());

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

                    coinsCache.addElement(new SimpleUser(player.getUniqueId(), player.getName(), 0.0));

                    player.sendMessage("§eEba! Agora você está em nosso banco de dados.");
                });
            } else {
                coinsCache.addElement(user);
            }
        });
    }

    @Override
    public void handleUserUnload(Player player) {
        Optional<User> byUniqueId = coinsCache.getByUniqueId(player.getUniqueId());
        if (!byUniqueId.isPresent()) {
            player.sendMessage("§cAlgo de errado aconteceu.");
            return;
        }

        coinsCache.removeElement(byUniqueId.get());
    }

    @Override
    public double getUserCoins(Player player) {
        Optional<User> byUniqueId = coinsCache.getByUniqueId(player.getUniqueId());
        if (!byUniqueId.isPresent()) {
            player.sendMessage("§cAlgo de errado aconteceu.");
            return 0.0;
        }

        return byUniqueId.map(User::getCoins).orElse(0.0);
    }

    @Override
    public double getUserCoins(String player) {
        Optional<User> byUniqueId = coinsCache.getByName(player);

        if (!byUniqueId.isPresent()) {
            return userController.getUserCoins(player);
        }

        return byUniqueId.map(User::getCoins).orElse(0.0);
    }
}
