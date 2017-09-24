package com.c2t2s.mc;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomizedBlockSelectionMenu implements Listener {

    private Inventory inventory;
    List<ItemStack> blocks;
    private Random random = new Random();
    private Map<String, MapInstance> maps;

    @SuppressWarnings("deprecation")
    public RandomizedBlockSelectionMenu(Map<String, MapInstance> maps) {
        this.maps = maps;
        ItemStack item;
        ItemMeta meta;
        blocks = new ArrayList<>();
        item = new ItemStack(Material.STONE, 1);
        meta = item.getItemMeta();
        meta.setDisplayName("Clear a Placed Block");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.ICE, 1);
        meta = item.getItemMeta();
        meta.setDisplayName("Slowness");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.DISPENSER, 1);
        meta = item.getItemMeta();
        meta.setDisplayName("Dispenser");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.QUARTZ_BLOCK, 1);
        meta = item.getItemMeta();
        meta.setDisplayName("Speed");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.STAINED_CLAY, 1, DyeColor.BLACK.getData());
        meta = item.getItemMeta();
        meta.setDisplayName("Blindness");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.STAINED_CLAY, 1, DyeColor.GREEN.getData());
        meta = item.getItemMeta();
        meta.setDisplayName("Poison");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.STAINED_CLAY, 1, DyeColor.LIME.getData());
        meta = item.getItemMeta();
        meta.setDisplayName("Jump Boost");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.STAINED_CLAY, 1, DyeColor.RED.getData());
        meta = item.getItemMeta();
        meta.setDisplayName("Instakill");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.STAINED_CLAY, 1, DyeColor.MAGENTA.getData());
        meta = item.getItemMeta();
        meta.setDisplayName("TP East (+x)");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.STAINED_CLAY, 1, DyeColor.ORANGE.getData());
        meta = item.getItemMeta();
        meta.setDisplayName("TP West (-x)");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.STAINED_CLAY, 1, DyeColor.PINK.getData());
        meta = item.getItemMeta();
        meta.setDisplayName("TP South (+z)");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.STAINED_CLAY, 1, DyeColor.PURPLE.getData());
        meta = item.getItemMeta();
        meta.setDisplayName("TP North (-z)");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.STAINED_CLAY, 1, DyeColor.WHITE.getData());
        meta = item.getItemMeta();
        meta.setDisplayName("TP to course start");
        item.setItemMeta(meta);
        blocks.add(item);
        item = new ItemStack(Material.STAINED_CLAY, 1, DyeColor.YELLOW.getData());
        meta = item.getItemMeta();
        meta.setDisplayName("Fire!!");
        item.setItemMeta(meta);
        blocks.add(item);
    }

    //TODO: Make this more dynamic for different list sizes
    public void generateInventory(int numberOfBlocks, boolean stone) {
        inventory = Bukkit.createInventory(null, 27, "Select a block");
        if (numberOfBlocks > 27) {
            numberOfBlocks = 27;
        }
        int slot = 0, type = 0;
        ItemStack item;
        while (numberOfBlocks > 0) {
            slot = random.nextInt(27);
            if (inventory.getItem(slot) == null || inventory.getItem(slot)
                    .getType().equals(Material.AIR)) {
                type = random.nextInt(18);
                if (type < 6) {
                    if (stone) {
                        inventory.setItem(slot, blocks.get(0));
                        numberOfBlocks--;
                    }
                } else {
                    inventory.setItem(slot, blocks.get(type - 5));
                    numberOfBlocks--;
                }
            }
        }
    }

    public void openInventory (Player player) {
        player.getInventory().clear();
        ItemStack stack = new ItemStack(Material.CHEST, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName("Block Selector");
        stack.setItemMeta(meta);
        player.getInventory().setItem(0, stack);
        player.openInventory(inventory);
    }

    public void openInventory (Collection<Player> players) {
        for (Player p : players) {
            ItemStack stack = new ItemStack(Material.CHEST, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName("Block Selector");
            stack.setItemMeta(meta);
            p.getInventory().clear();
            p.getInventory().setItem(0, stack);
            p.openInventory(inventory);
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onInventoryClickEvent(InventoryClickEvent e) {
        if (maps.containsKey(e.getWhoClicked().getWorld().getName())
                && maps.get(e.getWhoClicked().getWorld().getName()) != null
                && maps.get(e.getWhoClicked().getWorld().getName()).getPhase()
                .equals(MapInstance.Phase.BLOCKPLACING) && e.getInventory()
                .equals(inventory)) {
            e.setCancelled(true);
            if ((e.getSlotType().equals(InventoryType.SlotType.CONTAINER))
                    && e.getCurrentItem() != null
                    && !e.getCurrentItem().getType().equals(Material.AIR)
                    && !e.getCurrentItem().getType().equals(Material.CHEST)) {
                e.getWhoClicked().closeInventory();
                e.getWhoClicked().getInventory().setItem(0, new ItemStack(e
                        .getCurrentItem().getType(), 1, e.getCurrentItem()
                        .getData().getData()));
                inventory.setItem(e.getSlot(), new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        if (e.getItem() != null && e.getItem().getType().equals(Material.CHEST)
                && (e.getAction().equals(Action.RIGHT_CLICK_AIR)
                || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                && maps.containsKey(e.getPlayer().getWorld().getName())
                && maps.get(e.getPlayer().getWorld().getName()) != null
                && maps.get(e.getPlayer().getWorld().getName()).getPhase()
                .equals(MapInstance.Phase.BLOCKPLACING)) {
            openInventory(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
        if (maps.containsKey(e.getPlayer().getWorld().getName())
                && maps.get(e.getPlayer().getWorld().getName()) != null
                && maps.get(e.getPlayer().getWorld().getName()).getPhase()
                .equals(MapInstance.Phase.BLOCKPLACING)) {
            e.setCancelled(true);
        }
    }
}