package com.c2t2s.mc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public class LockedDispensers implements Listener {

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent e) {
        if (e.getInventory().getType().equals(InventoryType.DISPENSER)) {
            e.setCancelled(true);
        }
    }
}
