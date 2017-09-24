package com.c2t2s.mc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.Map;

public class LapisTeleporter implements Listener {

    private Map<String, MapInstance> worlds;

    public LapisTeleporter(Map<String, MapInstance> worlds) {
        this.worlds = worlds;
    }

    @EventHandler
    @SuppressWarnings("unchecked")
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        MapInstance world = worlds.get(e.getPlayer().getWorld().getName());
        if (world == null || world.isLapisDisabled() || !((Entity) e.getPlayer())
                .isOnGround() || !world.getWorld().getBlockAt(e.getPlayer()
                .getLocation().add(0.0, -0.5, 0.0)).getType().equals(
                Material.LAPIS_BLOCK)) {
            return;
        }
        if (world.getLapis() == null) {
            if (e.getPlayer().isOp()) {
                //[CHAT] Lapis not set error
                e.getPlayer().sendRawMessage(ChatColor.RED
                        + "No lapis location set");
            }
        } else {
            Player player = e.getPlayer();
            if (((Entity) player).isOnGround() && player.getWorld().getBlockAt(
                player.getLocation().add(0.0, -0.5, 0.0)).getType().equals(
                Material.LAPIS_BLOCK)) {
                player.teleport(world.getLapis());
            }
        }
    }
}
