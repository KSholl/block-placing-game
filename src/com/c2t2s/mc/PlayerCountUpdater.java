package com.c2t2s.mc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.Map;

public class PlayerCountUpdater implements Listener {

    private Map<String, MapInstance> maps;

    public PlayerCountUpdater (Map<String, MapInstance> maps) {
        this.maps = maps;
    }

    @EventHandler
    public void onPlayerJoinWorldEvent(PlayerChangedWorldEvent e) {
        if (maps.containsKey(e.getFrom().getName())) {
            MapInstance world = maps.get(e.getFrom().getName());
            world.getWaitingOn().remove(e.getPlayer());
            world.getPlayers().remove(e.getPlayer());
        }
        if (maps.containsKey(e.getPlayer().getWorld().getName())) {
            MapInstance map = maps.get(e.getPlayer().getWorld().getName());
            if (map != null ) {
                map.addPlayer(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        if (maps.containsKey(e.getPlayer().getWorld().getName())) {
            MapInstance map = maps.get(e.getPlayer().getWorld().getName());
            if (map != null) {
                map.removePlayer(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        if (maps.containsKey(e.getPlayer().getWorld().getName())) {
            MapInstance map = maps.get(e.getPlayer().getWorld().getName());
            if (map != null) {
                map.addPlayer(e.getPlayer());
            }
        }
    }
}
