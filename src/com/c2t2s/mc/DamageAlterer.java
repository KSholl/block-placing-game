package com.c2t2s.mc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import java.util.Map;
import java.util.logging.Level;

public class DamageAlterer implements Listener {

    private boolean cancelInNonGameWorlds;
    private Map<String, MapInstance> worlds;

    public DamageAlterer(boolean cancelInNonGameWorlds, Map<String, MapInstance>
            worlds) {
        this.cancelInNonGameWorlds = cancelInNonGameWorlds;
        this.worlds = worlds;
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && !e.isCancelled()) {
            Player player = (Player) e.getEntity();
            if (!worlds.keySet().contains(player.getWorld().getName()) || worlds
                    .get(player.getWorld().getName()) == null) {
                if (cancelInNonGameWorlds) {
                    e.setCancelled(true);
                }
            } else {
                switch (worlds.get(player.getWorld().getName()).getPhase()) {
                    case LOBBY:case LOBBYCOUNTDOWN:case BLOCKPLACING:
                        e.setCancelled(true);
                        break;
                    case SUDDENDEATH:case GAMEPLAY:
                        MapInstance world = worlds.get(player.getWorld()
                                .getName());
                        if (world.getWaitingOn().contains(player)) {
                            if (e.getCause().equals(EntityDamageEvent
                                    .DamageCause.FALL) && player.getWorld()
                                    .getBlockAt(player.getLocation()
                                            .add(0.0, -0.5, 0.0)).getType()
                                    .equals(Material.GLOWSTONE)) {
                                e.setCancelled(true);
                            } else if (player.getHealth()
                                    - e.getDamage() < 0) {
                                e.setCancelled(true);
                                EntityDamageEvent.DamageCause cause
                                        = e.getCause();
                                world.clearPotionEffect(player);
                                world.healPlayer(player);
                                if (cause.equals(EntityDamageEvent.DamageCause
                                        .FIRE_TICK)) {
                                    world.broadcastMessage(ChatColor.YELLOW
                                            + player.getName() + ChatColor.RED
                                            + " burned to death");
                                } else if (cause.equals(EntityDamageEvent
                                        .DamageCause.FALL) && !e
                                        .isCancelled()) {
                                    world.broadcastMessage(ChatColor.YELLOW
                                            + player.getName() + ChatColor.RED
                                            + " fell from a high place");
                                } else if (cause.equals(EntityDamageEvent
                                        .DamageCause.LAVA)) {
                                    world.broadcastMessage(ChatColor.YELLOW
                                            + player.getName() + ChatColor.RED
                                            + " tried to swim in lava");
                                } else if (cause.equals(EntityDamageEvent
                                        .DamageCause.MAGIC)) {
                                    world.broadcastMessage(ChatColor.YELLOW
                                            + player.getName() + ChatColor.RED
                                            + " poked the red block");
                                } else if (cause.equals(EntityDamageEvent
                                        .DamageCause.VOID)) {
                                    world.broadcastMessage(ChatColor.YELLOW
                                            + player.getName() + ChatColor.RED
                                            + " fell out of the world");
                                } else if (cause.equals(EntityDamageEvent
                                        .DamageCause.PROJECTILE)) {
                                    world.broadcastMessage(ChatColor.YELLOW
                                            + player.getName() + ChatColor.RED
                                            + " was crit out by a dispenser");
                                } else if (e instanceof EntityDamageByEntityEvent
                                        && ((EntityDamageByEntityEvent) e).getDamager() instanceof Boat) {
                                    world.broadcastMessage(ChatColor.YELLOW
                                            + player.getName() + ChatColor.RED
                                            + " died by lightning");
                                } else {
                                    world.broadcastMessage(ChatColor.YELLOW
                                            + player.getName() + ChatColor.RED
                                            + " died");
                                }
                                if (world.getPhase().equals(MapInstance.Phase
                                        .SUDDENDEATH)) {
                                    if (world.getStart() == null) {
                                        world.getParent().getLogger().log(Level
                                                .WARNING, "Start location for "
                                                + world.getWorld().getName()
                                                + " was null during deathmatch");
                                        world.warnOPs("Start location is null");
                                    } else {
                                        player.teleport(world.getStart());
                                    }
                                } else {
                                    if (world.getSpectator() == null) {
                                        world.getParent().getLogger().log(Level
                                                .WARNING, "Spectator location for"
                                                + world.getWorld().getName()
                                                + " was null during gameplay");
                                        world.warnOPs("Spectator location is null");
                                    } else {
                                        player.teleport(world.getSpectator());
                                    }
                                    world.getWaitingOn().remove(player);
                                }
                            }
                        }  else {
                            e.setCancelled(true);
                        }
                }
            }
        }
    }

    @EventHandler
    public void onEntityRegainHealthEvent(EntityRegainHealthEvent e) {
        if (e.getEntity() instanceof Player && worlds.containsKey(((Player)
                e.getEntity()).getWorld().getName())) {
            MapInstance world = worlds.get(((Player) e.getEntity()).getWorld()
                    .getName());
            if (world != null && (world.getPhase().equals(MapInstance.Phase
                    .GAMEPLAY) || world.getPhase().equals(MapInstance.Phase
                    .SUDDENDEATH) && world.getWaitingOn().contains((Player) e.getEntity()))) {
                e.setCancelled(true);
            }
        }
    }
}