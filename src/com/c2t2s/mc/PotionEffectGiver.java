package com.c2t2s.mc;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PotionEffectGiver implements Listener {

    private Map<Material, PotionEffect> blockEffects;
    private Map<Byte, PotionEffect> clayEffects;
    private Map<Byte, Vector> clayTPEffects;
    private Map<String, MapInstance> worlds;

    @SuppressWarnings("deprecation")
    public PotionEffectGiver (Map<String, MapInstance> worlds) {
        blockEffects = new HashMap<>();
        clayEffects = new HashMap<>();
        clayTPEffects = new HashMap<>();
        this.worlds = worlds;
        blockEffects.put(Material.ICE, new PotionEffect(
                PotionEffectType.SLOW, 60, 2, false,
                false));
        blockEffects.put(Material.QUARTZ_BLOCK, new PotionEffect(
                PotionEffectType.SPEED, 60, 1, false,
                false));
        blockEffects.put(Material.SOUL_SAND, new PotionEffect(
                PotionEffectType.JUMP, 60, 179, false,
                false));
        blockEffects.put(Material.NOTE_BLOCK, new PotionEffect(
                PotionEffectType.HEAL, 60, 49, false, false));
        blockEffects.put(Material.STAINED_CLAY, null);
        clayEffects.put(DyeColor.BLACK.getData(),  new PotionEffect(
                PotionEffectType.BLINDNESS, 60, 2, false,
                false));
        clayEffects.put(DyeColor.GREEN.getData(),  new PotionEffect(
                PotionEffectType.POISON, 60, 2, false,
                false));
        clayEffects.put(DyeColor.LIME.getData(),  new PotionEffect(
                PotionEffectType.JUMP, 60, 2, false,
                false));
        clayEffects.put(DyeColor.RED.getData(),  new PotionEffect(
                PotionEffectType.HARM, 60, 49, false,
                false));
        clayTPEffects.put(DyeColor.MAGENTA.getData(), new Vector(1, 0.2, 0));
        clayTPEffects.put(DyeColor.ORANGE.getData(),  new Vector(-1, 0.2, 0));
        clayTPEffects.put(DyeColor.PINK.getData(),  new Vector(0, 0.2, 1));
        clayTPEffects.put(DyeColor.PURPLE.getData(),  new Vector(0, 0.2, -1));
    }

    @EventHandler
    @SuppressWarnings({"deprecation", "unchecked"})
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        if (!worlds.containsKey(e.getPlayer().getWorld().getName())) {
            return;
        }
        Player player = e.getPlayer();
        MapInstance world = worlds.get(player.getWorld().getName());
        if (world == null || (!world.getPhase().equals(
                MapInstance.Phase.GAMEPLAY)) && !world.getPhase().equals(
                MapInstance.Phase.SUDDENDEATH)) {
            return;
        }
        if (((Entity) player).isOnGround()) {
            Block block = player.getWorld().getBlockAt(player.getLocation()
                    .add(0.0, -0.11, 0.0));
            if (blockEffects.containsKey(block.getType())) {
                if (block.getType().equals(Material.STAINED_CLAY)) {
                    if (clayEffects.containsKey(block.getData())) {
                        player.addPotionEffect(clayEffects.get(block.getData()),
                                true);
                    } else if (clayTPEffects.containsKey(block.getData())) {
                        player.setVelocity(clayTPEffects.get(block.getData()));
//                        player.teleport(player.getLocation().add(
//                                clayTPEffects.get(block.getData())));
                    } else if (block.getData() == DyeColor.YELLOW.getData()) {
                        player.setFireTicks(40);
                    } else if (block.getData() == DyeColor.WHITE.getData()) {
                        if (world.getStart() == null) {
                            world.getParent().getLogger().log(Level.WARNING,
                                    "Start location was null for " + world
                                    .getWorld().getName() + " during game "
                                    + "round");
                        } else {
                            player.teleport(world.getStart());
                        }
                    }
                } else {
                    player.addPotionEffect(blockEffects.get(block.getType()),
                            true);
                }
            } else if (block.getType().equals(Material.GOLD_BLOCK)) {
                if (world.getPhase().equals(MapInstance.Phase.SUDDENDEATH)) {
                    if (world.getWaitingOn().contains(player)) {
                        world.clearPotionEffects();
                        world.healPlayers();
                        world.getScoreManager().deathMatchEnd(player);
                    }
                } else {
                    if (world.getWaitingOn().contains(player)) {
                        world.getScoreManager().finished(player);
                        //TODO: [CHAT] Tell player they finished
                        world.getWaitingOn().remove(player);
                        world.healPlayer(player);
                        world.clearPotionEffect(player);
                        if (world.getSpectator() == null) {
                            world.getParent().getLogger().log(Level.WARNING,
                                    "Spectator location was null for " + world
                                    .getWorld().getName() + " during game "
                                    + "round");
                        } else {
                            player.teleport(world.getSpectator());
                        }
                    }
                }
            } else if (block.getType().equals(Material.LAPIS_BLOCK)) {
                if (world.getLapis() == null) {
                    world.getParent().getLogger().log(Level.WARNING,
                            "Lapis location was null for " + world
                                    .getWorld().getName() + " during game "
                                    + "round");
                }
                //Note: The tp is handled by the LapisTeleporter
            } else if (block.getType().equals(Material.WOOL)) {
                if (block.getData() == 4) { //yellow
                    //TODO: Decide what effect lightning should give a player
                    world.getWorld().strikeLightningEffect(player.getLocation());
                    if (player.getHealthScale() > 2) {
                        player.damage(2.0);
                    } else {
                        Entity entity = world.getWorld().spawnEntity(new
                                Location(world.getWorld(), 0.0, 0.0, 0.0), EntityType.BOAT);
                        player.damage(3.0, entity);
                        entity.remove();
                    }
                } else if (block.getData() == 14) { //red
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HARM,
                            60, 49, false, false), true);
                }
            }
            if (block.getType() != Material.SOUL_SAND) {
                for (PotionEffect effect: player.getActivePotionEffects()) {
                    if (effect.getType().equals(PotionEffectType.JUMP) && effect
                            .getAmplifier() == 179) {
                        player.removePotionEffect(PotionEffectType.JUMP);
                    }
                }
            } else {
                if (player.getWorld().getBlockAt(player.getLocation().add(0.0,
                        0.5, 0.0)).getType().equals(Material.LADDER) || player
                        .getWorld().getBlockAt(player.getLocation().add(0.0,
                        1.5, 0.0)).getType().equals(Material.LADDER)) {
                    player.removePotionEffect(PotionEffectType.JUMP);
                }
            }
        }
    }
}
