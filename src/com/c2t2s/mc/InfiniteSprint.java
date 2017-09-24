package com.c2t2s.mc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import java.util.Map;

public class InfiniteSprint implements Listener {

    private boolean infiniteSprintInNonGameWorlds;
    private Map<String, MapInstance> worlds;

    public InfiniteSprint(boolean infiniteSprintInNonGameWorlds,
                          Map<String, MapInstance> worlds) {
        this.infiniteSprintInNonGameWorlds = infiniteSprintInNonGameWorlds;
        this.worlds = worlds;
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            if (!worlds.containsKey(e.getEntity().getWorld().getName())
                    || worlds.get(e.getEntity().getWorld().getName()) == null) {
                if (infiniteSprintInNonGameWorlds) {
                    e.setCancelled(true);
                    Player player = (Player) e.getEntity();
                    if (player.getFoodLevel() != 20) {
                        player.setFoodLevel(20);
                        player.setSaturation(100);
                    } else {
                        player.setSaturation(100);
                    }
                }
            } else {
                e.setCancelled(true);
                Player player = (Player) e.getEntity();
                if (player.getFoodLevel() != 20) {
                    player.setFoodLevel(20);
                    player.setSaturation(100);
                } else {
                    player.setSaturation(100);
                }
            }
        }
    }
}
