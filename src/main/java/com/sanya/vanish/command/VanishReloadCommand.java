package com.sanya.vanish.command;

import com.sanya.vanish.VanishPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class VanishReloadCommand implements CommandExecutor {

    private final VanishPlugin plugin;

    public VanishReloadCommand(VanishPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vanish.reload")) {
            sender.sendMessage(plugin.prefixedMessage("messages.no-permission-reload", "&cУ тебя нет прав: &fvanish.reload"));
            return true;
        }

        plugin.reloadPluginConfig();
        sender.sendMessage(plugin.prefixedMessage("messages.reloaded", "&aКонфиг Vanish перезагружен."));
        return true;
    }
}
