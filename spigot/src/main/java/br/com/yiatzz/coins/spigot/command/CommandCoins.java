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

        switch (args.length) {
            case 0:
                if (!(sender instanceof Player)) {
                    break;
                }

                provider.get().handleUserInfoPerfomed((Player) sender);
                break;
            case 1:
                if (!(sender instanceof Player)) {
                    break;
                }

                Player player = (Player) sender;

                if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("set")) {
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
                    sender.sendMessage("§cUsuário inválido.");
                    break;
                }


                OptionalDouble optionalDouble = OptionalNumber.tryParseDouble(args[2]);
                if (!optionalDouble.isPresent() || args[2].equalsIgnoreCase("nan")) {
                    sender.sendMessage("§cQuantia inválida.");
                    return false;
                }

                double value = optionalDouble.getAsDouble();
                if (value <= 0.0 || value <= 0) {
                    sender.sendMessage("§cValor inválido.");
                    return false;
                }

                boolean hasPermission = sender.hasPermission("coins.admin");

                switch (args[0].toLowerCase()) {
                    case "give":
                    case "add":
                    case "adicionar":
                        if (!hasPermission) {
                            sender.sendMessage("§cVocê não tem permissão para executar este comando.");
                            break;
                        }

                        provider.get().handleUserDepositPerfomed(otherPlayer, value);
                        break;
                    case "remove":
                    case "remover":
                        if (!hasPermission) {
                            sender.sendMessage("§cVocê não tem permissão para executar este comando.");
                            break;
                        }

                        provider.get().handleUserCoinsWithdrawPerfomed(otherPlayer, value);
                        break;
                    case "set":
                    case "define":
                    case "definir":
                        if (!hasPermission) {
                            sender.sendMessage("§cVocê não tem permissão para executar este comando.");
                            break;
                        }

                        provider.get().handleUserDefinedCoins(otherPlayer, value);
                        break;
                    case "pay":
                    case "enviar":
                    case "pagar":
                        if (!(sender instanceof Player)) {
                            break;
                        }

                        provider.get().handleUserPaymentPerfomed((Player) sender, otherPlayer, value);
                        break;
                }
                break;
        }

        return true;
    }
}
