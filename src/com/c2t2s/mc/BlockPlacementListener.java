package com.c2t2s.mc;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockPlacementListener implements Listener {

    private Map<String, MapInstance> worlds;

    public BlockPlacementListener (Map<String, MapInstance> worlds) {
        this.worlds = worlds;
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        MapInstance world = worlds.get(e.getPlayer().getWorld().getName());
        if (world == null || !world.getPhase().equals(MapInstance.Phase.
                BLOCKPLACING) || e.isCancelled() || !e.getHand().equals(
                EquipmentSlot.HAND)) {
            return;
        }
        e.setCancelled(true);
        Material itemInHand = e.getPlayer().getItemInHand().getType();
        if (itemInHand.equals(Material.STAINED_CLAY)
                && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (!e.getClickedBlock().getType().equals(Material.STONE)
                    || !e.getBlockFace().equals(BlockFace.UP)) {
                //[CHAT] Error message for placing block not on stone
                e.getPlayer().sendRawMessage(ChatColor.RED + "You can only "
                        + "place that on top of stone");
                //TODO: Play noise?
            } else {
                e.getClickedBlock().setType(itemInHand);
                world.getPlacedBlocks().add(e.getClickedBlock().getLocation());
                e.getClickedBlock().setData(e.getPlayer().getItemInHand()
                        .getData().getData(), true);
                e.getPlayer().getInventory().setItemInHand(null);
                world.getWaitingOn().remove(e.getPlayer());
            }
        } else if ((itemInHand.equals(Material.ICE)
                || itemInHand.equals(Material.SOUL_SAND)
                || itemInHand.equals(Material.QUARTZ_BLOCK))
                && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (!e.getClickedBlock().getType().equals(Material.STONE)
                    || !e.getBlockFace().equals(BlockFace.UP)) {
                //[CHAT] Error message for placing block not on stone
                e.getPlayer().sendRawMessage(ChatColor.RED + "You can only "
                        + "place that on top of stone");
                //TODO: Play noise?
            } else {
                e.getClickedBlock().setType(itemInHand);
                world.getPlacedBlocks().add(e.getClickedBlock().getLocation());
                e.getPlayer().getInventory().setItemInHand(null);
                world.getWaitingOn().remove(e.getPlayer());
            }
        } else if (itemInHand.equals(Material.DISPENSER)
                && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            BlockFace sideClicked = e.getBlockFace();
            if (!e.getClickedBlock().getType().equals(Material.WOOL)
                    || e.getClickedBlock().getData() != DyeColor.BLACK.getData()
                    || !(sideClicked.equals(BlockFace.NORTH)
                    || sideClicked.equals(BlockFace.EAST)
                    || sideClicked.equals(BlockFace.SOUTH)
                    || sideClicked.equals(BlockFace.WEST))) {
                //[CHAT] Error message for placing dispenser not on wool
                e.getPlayer().sendRawMessage(ChatColor.RED + "You can only "
                        + "place that on the side of black wool");
                //TODO: Play sound?
            } else {
                e.getClickedBlock().getRelative(sideClicked)
                        .setType(Material.DISPENSER);
                world.getPlacedBlocks().add(e.getClickedBlock().getRelative(
                        sideClicked).getLocation());
                Dispenser dispenserState = new Dispenser();
                dispenserState.setFacingDirection(sideClicked);
                BlockState state = e.getClickedBlock().getRelative(
                        sideClicked).getState();
                state.setData(dispenserState);
                state.update(true);
                e.getPlayer().getInventory().setItemInHand(null);
                world.getWaitingOn().remove(e.getPlayer());
                if (sideClicked.equals(BlockFace.EAST)) {
                    world.getArrows().put(e.getClickedBlock().getRelative(
                            sideClicked).getLocation(), new
                            ArrowSpawnLocation(e.getClickedBlock().getLocation()
                            .add(2.3, 0.5, 0.5), new Vector(1.0, 0.0, 0.0)));
                } else if (sideClicked.equals(BlockFace.WEST)) {
                    world.getArrows().put(e.getClickedBlock().getRelative(
                            sideClicked).getLocation(), new
                            ArrowSpawnLocation(e.getClickedBlock().getLocation()
                            .add(-1.1, 0.5, 0.5), new Vector(-1.0, 0.0, 0.0)));
                } else if (sideClicked.equals(BlockFace.NORTH)) {
                    world.getArrows().put(e.getClickedBlock().getRelative(
                            sideClicked).getLocation(), new
                            ArrowSpawnLocation(e.getClickedBlock().getLocation()
                            .add(0.5, 0.5, -1.1), new Vector(0.0, 0.0, -1.0)));
                } else if (sideClicked.equals(BlockFace.SOUTH)) {
                    world.getArrows().put(e.getClickedBlock().getRelative(
                            sideClicked).getLocation(), new ArrowSpawnLocation(e
                            .getClickedBlock().getLocation().add(0.5, 0.5, 2.3),
                            new Vector(0.0, 0.0, 1.0)));
                }
            }
        } else if (itemInHand.equals(Material.STONE) && e.getAction().equals(
                Action.RIGHT_CLICK_BLOCK)) {
            if (e.getPlayer().getItemInHand().getData().getData() == 3) {
                List<ArmorStand> armorStands = new ArrayList<>();
                ArmorStand armorStand = (ArmorStand) world.getWorld().spawnEntity(
                        e.getClickedBlock().getLocation().add(+0.3129, -1.0, +0.3129),
                        EntityType.ARMOR_STAND);
                world.getWorld().getBlockAt(e.getClickedBlock().getLocation()).setType(Material.AIR);
                armorStand.setHelmet(new ItemStack(Material.STONE, 1, (short) 1, (byte) 3));
                armorStand.setBasePlate(false);
                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStands.add(armorStand);
                armorStand = (ArmorStand) world.getWorld().spawnEntity(
                        e.getClickedBlock().getLocation().add(+0.6875, -0.999, +0.3130),
                        EntityType.ARMOR_STAND);
                armorStand.setHelmet(new ItemStack(Material.STONE, 1, (short) 1, (byte) 3));
                armorStand.setBasePlate(false);
                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStands.add(armorStand);
                armorStand = (ArmorStand) world.getWorld().spawnEntity(
                        e.getClickedBlock().getLocation().add(+0.3130, -0.998, +0.6875),
                        EntityType.ARMOR_STAND);
                armorStand.setHelmet(new ItemStack(Material.STONE, 1, (short) 1, (byte) 3));
                armorStand.setBasePlate(false);
                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStands.add(armorStand);
                armorStand = (ArmorStand) world.getWorld().spawnEntity(
                        e.getClickedBlock().getLocation().add(+0.6874, -0.997, +0.6874),
                        EntityType.ARMOR_STAND);
                armorStand.setHelmet(new ItemStack(Material.STONE, 1, (short) 1, (byte) 3));
                armorStand.setBasePlate(false);
                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStands.add(armorStand);
                armorStand = (ArmorStand) world.getWorld().spawnEntity(
                        e.getClickedBlock().getLocation().add(+0.3127, -1.375, +0.3127),
                        EntityType.ARMOR_STAND);
                world.getWorld().getBlockAt(e.getClickedBlock().getLocation()).setType(Material.AIR);
                armorStand.setHelmet(new ItemStack(Material.STONE, 1, (short) 1, (byte) 3));
                armorStand.setBasePlate(false);
                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStands.add(armorStand);
                armorStand = (ArmorStand) world.getWorld().spawnEntity(
                        e.getClickedBlock().getLocation().add(+0.6873, -1.374, +0.3128),
                        EntityType.ARMOR_STAND);
                armorStand.setHelmet(new ItemStack(Material.STONE, 1, (short) 1, (byte) 3));
                armorStand.setBasePlate(false);
                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStands.add(armorStand);
                armorStand = (ArmorStand) world.getWorld().spawnEntity(
                        e.getClickedBlock().getLocation().add(+0.3128, -1.373, +0.6873),
                        EntityType.ARMOR_STAND);
                armorStand.setHelmet(new ItemStack(Material.STONE, 1, (short) 1, (byte) 3));
                armorStand.setBasePlate(false);
                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStands.add(armorStand);
                armorStand = (ArmorStand) world.getWorld().spawnEntity(
                        e.getClickedBlock().getLocation().add(+0.6872, -1.372, +0.6872),
                        EntityType.ARMOR_STAND);
                armorStand.setHelmet(new ItemStack(Material.STONE, 1, (short) 1, (byte) 3));
                armorStand.setBasePlate(false);
                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStands.add(armorStand);
                world.getArmorStands().put(e.getClickedBlock().getLocation(), armorStands);
            } else {
                BlockFace sideClicked = e.getBlockFace();
                if (sideClicked.equals(BlockFace.UP)) {
                    Material blockType = e.getClickedBlock().getType();
                    if (blockType.equals(Material.STAINED_CLAY)) {
                        byte data = e.getClickedBlock().getData();
                        if (data == DyeColor.BLACK.getData() || data
                                == DyeColor.GREEN.getData() || data
                                == DyeColor.LIME.getData() || data
                                == DyeColor.RED.getData() || data
                                == DyeColor.MAGENTA.getData() || data
                                == DyeColor.ORANGE.getData() || data
                                == DyeColor.PINK.getData() || data
                                == DyeColor.PURPLE.getData() || data
                                == DyeColor.WHITE.getData() || data
                                == DyeColor.YELLOW.getData()) {
                            if (!world.getPlacedBlocks().remove(e.getClickedBlock()
                                    .getLocation())) {
                                world.getExistingBlocks().put(e.getClickedBlock()
                                        .getLocation(), e.getClickedBlock()
                                        .getState().getData());
                            }
                            e.getClickedBlock().setType(Material.STONE);
                            e.getPlayer().getInventory().setItemInHand(null);
                            world.getWaitingOn().remove(e.getPlayer());
                        } else {
                            //[CHAT] Error message for placing stone wrong
                            e.getPlayer().sendRawMessage(ChatColor.RED
                                    + "You can only place that "
                                    + "on top of another placeable block or beside "
                                    + "a dispenser");
                            //TODO: Play noise?
                        }
                    } else if (blockType.equals(Material.ICE) || blockType
                            .equals(Material.QUARTZ_BLOCK)) {
                        if (!world.getPlacedBlocks().remove(e.getClickedBlock()
                                .getLocation())) {
                            world.getExistingBlocks().put(e.getClickedBlock()
                                    .getLocation(), e.getClickedBlock()
                                    .getState().getData());
                        }
                        e.getClickedBlock().setType(Material.STONE);
                        e.getPlayer().getInventory().setItemInHand(null);
                        world.getWaitingOn().remove(e.getPlayer());
                    } else {
                        //[CHAT] Error message for placing stone wrong
                        e.getPlayer().sendRawMessage("You can only place that "
                                + "on top of another placeable block or beside "
                                + "a dispenser");
                        //TODO: Play noise?
                    }

                } else if (e.getClickedBlock().getType().equals(Material.DISPENSER)
                        && !(sideClicked.equals(BlockFace.DOWN)
                        || sideClicked.equals(BlockFace.UP))) {
                    if (!world.getPlacedBlocks().remove(e.getClickedBlock()
                            .getLocation())) {
                        world.getExistingBlocks().put(e.getClickedBlock()
                                .getLocation(), e.getClickedBlock()
                                .getState().getData());
                    }
                    if (world.getArrows().remove(e.getClickedBlock().getLocation()) == null) {
                        //TODO: Test if it was a fireball or other thingy

                    }
                    e.getClickedBlock().setType(Material.AIR);
                    e.getPlayer().getInventory().setItemInHand(null);
                    world.getWaitingOn().remove(e.getPlayer());
                } else {
                    //[CHAT] Error message for placing stone wrong
                    e.getPlayer().sendRawMessage("You can only place that "
                            + "on top of another placeable block or beside "
                            + "a dispenser");
                    //TODO: Play noise?
                }
            }
        } else if (itemInHand.equals(Material.FIREBALL)
                && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            //TODO: Make this not a direct copy
            BlockFace sideClicked = e.getBlockFace();
            if (!e.getClickedBlock().getType().equals(Material.WOOL)
                    || e.getClickedBlock().getData() != DyeColor.BLACK.getData()
                    || !(sideClicked.equals(BlockFace.NORTH)
                    || sideClicked.equals(BlockFace.EAST)
                    || sideClicked.equals(BlockFace.SOUTH)
                    || sideClicked.equals(BlockFace.WEST))) {
                //[CHAT] Error message for placing dispenser not on wool
                e.getPlayer().sendRawMessage(ChatColor.RED + "You can only "
                        + "place that on the side of black wool");
                //TODO: Play sound?
            } else {
                e.getClickedBlock().getRelative(sideClicked)
                        .setType(Material.DISPENSER);
                world.getPlacedBlocks().add(e.getClickedBlock().getRelative(
                        sideClicked).getLocation());
                Dispenser dispenserState = new Dispenser();
                dispenserState.setFacingDirection(sideClicked);
                BlockState state = e.getClickedBlock().getRelative(
                        sideClicked).getState();
                state.setData(dispenserState);
                state.update(true);
                e.getPlayer().getInventory().setItemInHand(null);
                world.getWaitingOn().remove(e.getPlayer());
                if (sideClicked.equals(BlockFace.EAST)) {
                    world.getFireballs().put(e.getClickedBlock().getLocation()
                            .add(2.3, 0.5, 0.5), new Vector(1.0, 0.0, 0.0));
                } else if (sideClicked.equals(BlockFace.WEST)) {
                    world.getFireballs().put(e.getClickedBlock().getLocation()
                            .add(-1.1, 0.5, 0.5), new Vector(-1.0, 0.0, 0.0));
                } else if (sideClicked.equals(BlockFace.NORTH)) {
                    world.getFireballs().put(e.getClickedBlock().getLocation()
                            .add(0.5, 0.5, -1.1), new Vector(0.0, 0.0, -1.0));
                } else if (sideClicked.equals(BlockFace.SOUTH)) {
                    world.getFireballs().put(e.getClickedBlock().getLocation()
                            .add(0.5, 0.5, 2.3), new Vector(0.0, 0.0, 1.0));
                }
            }
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e) {
        System.out.print("Message 1");
//        e.setCancelled(true);
//        MapInstance world = worlds.get(e.getPlayer().getWorld().getName());
//        if (world != null && world.getPhase().equals(MapInstance.Phase.BLOCKPLACING)
//                && e.getPlayer().getItemInHand().getType().equals(Material.STONE)
//                && e.getRightClicked().getHelmet().getType().equals(Material.STONE)) {
//            System.out.print("Message 2");
//            List<ArmorStand> armorStands = world.getArmorStands().remove(
//                    world.getWorld().getBlockAt(e.getRightClicked()
//                    .getLocation().add(0.0, 1.5, 0.0)).getLocation());
//            if (armorStands != null) {
//                for (ArmorStand armorStand: armorStands) {
//                    if (!armorStand.isDead()) {
//                        armorStand.remove();
//                    }
//                }
//            }
//        }
    }
}