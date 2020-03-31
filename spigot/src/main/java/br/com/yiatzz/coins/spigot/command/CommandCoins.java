package br.com.yiatzz.coins.spigot.command;

import br.com.yiatzz.coins.spigot.CoinsSpigotApplication;
import br.com.yiatzz.coins.spigot.util.OptionalNumber;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.OptionalDouble;

public class CommandCoins implements CommandExecutor {

    private Provider<CoinsSpigotApplication> provider;

    @Inject
    public CommandCoins(Provider<CoinsSpigotApplication> provider) {
        this.provider = provider;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        switch (args.length) {
            case 0:
                provider.get().handleUserInfoPerfomed(player);
                break;
            case 1:
                if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("set")) {
                    if (!player.hasPermission("coins.admin")) {
                        player.sendMessage("§cVocê não tem permissão para executar este comando.");
                        break;
                    }

                    player.sendMessage("§cArgumentos inválidos.");
                    break;
                } else if (args[0].equalsIgnoreCase("top")) {
                    provider.get().handleRankInfoPerfomed(player);
                    break;
                }

                provider.get().handleUserViewingInfoPerfomed(player, args[0]);
                break;
            case 3:
                Player otherPlayer = Bukkit.getPlayer(args[1]);

                if (otherPlayer == null) {
                    player.sendMessage("§cUsuário inválido.");
                    break;
                }


                OptionalDouble optionalDouble = OptionalNumber.tryParseDouble(args[2]);
                if (!optionalDouble.isPresent() || args[2].equalsIgnoreCase("nan")) {
                    player.sendMessage("§cQuantia inválida.");
                    return false;
                }

                double value = optionalDouble.getAsDouble();
                boolean hasPermission = player.hasPermission("coins.admin");

                switch (args[0].toLowerCase()) {
                    case "add":
                    case "adicionar":
                        if (!hasPermission) {
                            player.sendMessage("§cVocê não tem permissão para executar este comando.");
                            break;
                        }

                        provider.get().handleUserDepositPerfomed(otherPlayer, value);
                        break;
                    case "remove":
                    case "remover":
                        if (!hasPermission) {
                            player.sendMessage("§cVocê não tem permissão para executar este comando.");
                            break;
                        }

                        provider.get().handleUserCoinsWithdrawPerfomed(otherPlayer, value);
                        break;
                    case "set":
                    case "define":
                    case "definir":
                        if (!hasPermission) {
                            player.sendMessage("§cVocê não tem permissão para executar este comando.");
                            break;
                        }

                        provider.get().handleUserDefinedCoins(otherPlayer, value);
                        break;
                    case "pay":
                    case "enviar":
                    case "pagar":
                        provider.get().handleUserPaymentPerfomed(player, otherPlayer, value);
                        break;
                }
                break;
        }

        return true;
    }
}
