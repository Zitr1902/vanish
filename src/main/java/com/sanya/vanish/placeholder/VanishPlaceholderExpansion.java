package com.sanya.vanish.placeholder;

import com.sanya.vanish.VanishPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public final class VanishPlaceholderExpansion extends PlaceholderExpansion {

    private final VanishPlugin plugin;

    public VanishPlaceholderExpansion(VanishPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "vanish";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("online")) {
            return String.valueOf(plugin.getAdjustedOnlineCount());
        }

        if (params.equalsIgnoreCase("vanished")) {
            return String.valueOf(plugin.getVanishedPlayers().size());
        }

        return null;
    }
}
