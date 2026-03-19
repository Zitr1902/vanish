package com.sanya.vanish.listener;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.sanya.vanish.VanishPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerConnectionListener implements Listener {

    private final VanishPlugin plugin;

    public PlayerConnectionListener(VanishPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        plugin.prepareJoinMetadata(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.handleJoin(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.handleQuit(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerListPing(PaperServerListPingEvent event) {
        plugin.handleServerListPing(event);
    }
}
