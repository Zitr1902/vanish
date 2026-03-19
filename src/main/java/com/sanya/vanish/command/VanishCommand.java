package com.sanya.vanish.command;

import com.sanya.vanish.VanishPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class VanishCommand implements CommandExecutor {

    private final VanishPlugin plugin;

    public VanishCommand(VanishPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("vanish.use")) {
            player.sendMessage(plugin.prefixedMessage("messages.no-permission", "&cУ тебя нет прав: &fvanish.use"));
            return true;
        }

        if (args.length > 1) {
            return false;
        }

        Player teleportTarget = null;
        if (args.length == 1) {
            teleportTarget = Bukkit.getPlayerExact(args[0]);
            if (teleportTarget == null) {
                player.sendMessage(plugin.prefixedMessage("messages.target-not-found", "&cИгрок не найден."));
                return true;
            }
        }

        boolean enabled = plugin.toggleVanish(player, teleportTarget);
        if (enabled) {
            player.sendMessage(plugin.prefixedMessage("messages.enabled", "&aВаниш включен"));
            if (teleportTarget != null) {
                player.sendMessage(plugin.prefixedMessage("messages.teleported", "&aТы телепортирован к игроку: &f%target%")
                        .replace("%target%", teleportTarget.getName()));
            }
        } else {
            player.sendMessage(plugin.prefixedMessage("messages.disabled", "&cВаниш выключен"));
        }

        return true;
    }
}
