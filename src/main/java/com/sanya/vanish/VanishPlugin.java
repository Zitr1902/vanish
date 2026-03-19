package com.sanya.vanish;

import com.sanya.vanish.command.VanishCommand;
import com.sanya.vanish.command.VanishReloadCommand;
import com.sanya.vanish.listener.PlayerConnectionListener;
import com.sanya.vanish.placeholder.VanishPlaceholderExpansion;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VanishPlugin extends JavaPlugin {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final String VANISHED_METADATA = "vanished";
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final Map<UUID, Location> returnLocations = new HashMap<>();
    private final Map<UUID, Boolean> previousAllowFlight = new HashMap<>();
    private final Map<UUID, Boolean> previousFlying = new HashMap<>();
    private final Map<UUID, GameMode> previousGameMode = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        VanishCommand vanishCommand = new VanishCommand(this);
        if (getCommand("vanish") != null) {
            getCommand("vanish").setExecutor(vanishCommand);
        }
        VanishReloadCommand reloadCommand = new VanishReloadCommand(this);
        if (getCommand("vanishreload") != null) {
            getCommand("vanishreload").setExecutor(reloadCommand);
        }

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new VanishPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI found. Placeholder %vanish_online% registered.");
        }
    }

    @Override
    public void onDisable() {
        for (UUID uuid : new HashSet<>(vanishedPlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                disableVanish(player, false);
            }
        }

        vanishedPlayers.clear();
        returnLocations.clear();
        previousAllowFlight.clear();
        previousFlying.clear();
        previousGameMode.clear();
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public Set<UUID> getVanishedPlayers() {
        return Collections.unmodifiableSet(vanishedPlayers);
    }

    public int getAdjustedOnlineCount() {
        return Math.max(0, Bukkit.getOnlinePlayers().size() - getOnlineVanishedCount());
    }

    public boolean toggleVanish(Player player, Player teleportTarget) {
        if (isVanished(player)) {
            disableVanish(player);
            return false;
        }

        enableVanish(player, teleportTarget);
        return true;
    }

    public void enableVanish(Player player, Player teleportTarget) {
        UUID uuid = player.getUniqueId();
        vanishedPlayers.add(uuid);
        markVanished(player);

        returnLocations.putIfAbsent(uuid, player.getLocation().clone());
        previousAllowFlight.putIfAbsent(uuid, player.getAllowFlight());
        previousFlying.putIfAbsent(uuid, player.isFlying());
        previousGameMode.putIfAbsent(uuid, player.getGameMode());

        if (getConfig().getBoolean("vanish.enable-flight", true)) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        if (getConfig().getBoolean("vanish.no-clip-spectator", true)) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        if (teleportTarget != null && !teleportTarget.equals(player)) {
            player.teleport(teleportTarget.getLocation());
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) {
                continue;
            }

            if (!online.hasPermission("vanish.see")) {
                online.hidePlayer(this, player);
            }
        }

        String fakeQuit = colorize(getConfig().getString("broadcast.fake-quit", "&e%player% вышел с сервера"));
        Bukkit.broadcastMessage(fakeQuit.replace("%player%", player.getName()));
    }

    public void disableVanish(Player player) {
        disableVanish(player, true);
    }

    private void disableVanish(Player player, boolean broadcast) {
        UUID uuid = player.getUniqueId();
        vanishedPlayers.remove(uuid);
        unmarkVanished(player);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) {
                continue;
            }

            online.showPlayer(this, player);
        }

        Location returnLocation = returnLocations.remove(uuid);
        if (returnLocation != null) {
            player.teleport(returnLocation);
        }

        Boolean oldAllowFlight = previousAllowFlight.remove(uuid);
        Boolean oldFlying = previousFlying.remove(uuid);
        GameMode oldGameMode = previousGameMode.remove(uuid);

        if (oldGameMode != null) {
            player.setGameMode(oldGameMode);
        }

        if (oldAllowFlight != null) {
            player.setAllowFlight(oldAllowFlight);
            if (oldAllowFlight) {
                player.setFlying(Boolean.TRUE.equals(oldFlying));
            } else {
                player.setFlying(false);
            }
        }
        if (broadcast) {
            String fakeJoin = colorize(getConfig().getString("broadcast.fake-join", "&e%player% зашел на сервер"));
            Bukkit.broadcastMessage(fakeJoin.replace("%player%", player.getName()));
        }
    }

    public void applyVanishForViewer(Player viewer) {
        for (UUID uuid : vanishedPlayers) {
            Player vanished = Bukkit.getPlayer(uuid);
            if (vanished == null || vanished.equals(viewer)) {
                continue;
            }

            if (!viewer.hasPermission("vanish.see")) {
                viewer.hidePlayer(this, vanished);
            }
        }

        if (isVanished(viewer)) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.equals(viewer)) {
                    continue;
                }

                if (!online.hasPermission("vanish.see")) {
                    online.hidePlayer(this, viewer);
                }
            }
        }
    }

    public void handleJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        applyVanishForViewer(player);

        if (!isVanished(player)) {
            unmarkVanished(player);
            return;
        }

        markVanished(player);
        event.joinMessage(null);
        event.setJoinMessage(null);

        if (getConfig().getBoolean("vanish.enable-flight", true)) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        if (getConfig().getBoolean("vanish.no-clip-spectator", true)) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        player.sendMessage(prefixedMessage("messages.still-vanished", "&eВы по-прежнему невидимы."));
    }

    public void handleQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!vanishedPlayers.contains(uuid)) {
            return;
        }

        GameMode oldGameMode = previousGameMode.get(uuid);
        if (oldGameMode != null) {
            player.setGameMode(oldGameMode);
        }

        Boolean oldAllowFlight = previousAllowFlight.get(uuid);
        Boolean oldFlying = previousFlying.get(uuid);
        if (oldAllowFlight != null) {
            player.setAllowFlight(oldAllowFlight);
            if (oldAllowFlight) {
                player.setFlying(Boolean.TRUE.equals(oldFlying));
            } else {
                player.setFlying(false);
            }
        }

        event.quitMessage(null);
        event.setQuitMessage(null);
    }

    public void handleServerListPing(PaperServerListPingEvent event) {
        event.setNumPlayers(getAdjustedOnlineCount());
        event.getListedPlayers().removeIf(info -> vanishedPlayers.contains(info.id()));
    }

    public void prepareJoinMetadata(Player player) {
        if (isVanished(player)) {
            markVanished(player);
        } else {
            unmarkVanished(player);
        }
    }

    public void reloadPluginConfig() {
        reloadConfig();
    }

    public String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String withHex = applyHexColors(text);
        return ChatColor.translateAlternateColorCodes('&', withHex);
    }

    public String prefixedMessage(String key, String fallback) {
        String prefix = colorize(getConfig().getString("messages.prefix", "&8[&bVanish&8] "));
        String body = colorize(getConfig().getString(key, fallback));
        return prefix + body;
    }

    private String applyHexColors(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder output = new StringBuilder();

        while (matcher.find()) {
            String hex = "#" + matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of(hex).toString();
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(output);
        return output.toString();
    }

    private int getOnlineVanishedCount() {
        int count = 0;
        for (UUID uuid : vanishedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                count++;
            }
        }
        return count;
    }

    private void markVanished(Player player) {
        player.setMetadata(VANISHED_METADATA, new FixedMetadataValue(this, true));
    }

    private void unmarkVanished(Player player) {
        player.removeMetadata(VANISHED_METADATA, this);
    }
}
