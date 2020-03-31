package br.com.yiatzz.coins.spigot;

import org.bukkit.entity.Player;

public interface CoinsSpigotApplication {

    void initialize();

    void destroy();

    void hookVault();

    void convert();

    void handleUserInfoPerfomed(Player player);

    void handleUserViewingInfoPerfomed(Player player, String target);

    void handleUserDepositPerfomed(Player player, double newValue);

    void handleUserCoinsWithdrawPerfomed(Player player, double newValue);

    void handleUserPaymentPerfomed(Player player, Player target, double value);

    void handleUserDefinedCoins(Player player, double newValue);

    void handleRankInfoPerfomed(Player player);

    double getUserCoins(Player player);

    double getUserCoins(String player);

    /*

     */

    void handleUserLoadInfos(Player player);

}
