package com.c2t2s.mc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Map;
import java.util.logging.Level;

public class CommandHandler implements CommandExecutor {

    private Main parent;
    private Map<String, MapInstance> maps;

    public CommandHandler(Main parent, Map<String, MapInstance> maps) {
        this.parent = parent;
        this.maps = maps;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
                             String[] args) {
        if (sender instanceof Player && args.length == 2) {
            Player player = (Player) sender;
            if (!player.isOp()) {
                player.sendRawMessage(ChatColor.RED
                        + "You must be opped to use that command");
                return true;
            } else if (!maps.keySet().contains(player.getWorld().getName())) {
                player.sendRawMessage(ChatColor.RED + "You must be in a game "
                        + "world to use that command. Use /bg registerWorld to"
                        + " register a world");
                return true;
            }
            MapInstance world = maps.get(player.getWorld().getName());
            if (world == null) {
                parent.getLogger().log(Level.WARNING, "MapInstance for "
                        + player.getWorld().getName() + " was null. Clearing.");
                player.sendRawMessage(ChatColor.RED + "World not properly "
                        + "loaded. Use /bg registerWorld to reload");
                return true;
            }
            else if (args[0].equals("set")) {
                if (args[1].equals("lobby") || args[1].equals("spawn")) {
                    world.setLobby(player.getLocation());
                    player.sendRawMessage(ChatColor.GREEN + "Lobby set");
                } else if (args[1].equals("start")) {
                    world.setStart(player.getLocation());
                    player.sendRawMessage(ChatColor.GREEN + "Start set");
                } else if (args[1].equals("lapis")) {
                    world.setLapis(player.getLocation());
                    player.sendRawMessage(ChatColor.GREEN + "Lapis set");
                } else if (args[1].equals("spectate") || args[1]
                        .equals("spectator")) {
                    world.setSpectator(player.getLocation());
                    player.sendRawMessage(ChatColor.GREEN + "Spectator set");
                } else {
                    player.sendRawMessage(ChatColor.RED + "Usage: /bg set "
                            + "[lobby/start/lapis/spectator]");

                }
                return true;
            } else if (args[0].equals("test") || args[0].equals("visit")) {
                switch (args[1]) {
                    case "lobby":case "spawn":
                        if (world.getLobby() == null) {
                            player.sendRawMessage(ChatColor.RED + "No lobby location set");
                        } else {
                            player.teleport(world.getLobby());
                            player.sendRawMessage(ChatColor.GREEN + "Teleported to lobby");
                        }
                        return true;
                    case "start":
                        if (world.getStart() == null) {
                            player.sendRawMessage(ChatColor.RED + "No start location set");
                        } else {
                            player.teleport(world.getStart());
                            player.sendRawMessage(ChatColor.GREEN + "Teleported to start");
                        }
                        return true;
                    case "lapis":
                        if (world.getLapis() == null) {
                            player.sendRawMessage(ChatColor.RED + "No lapis location set");
                        } else {
                            player.teleport(world.getLapis());
                            player.sendRawMessage(ChatColor.GREEN + "Teleported to lapis");
                        }
                        return true;
                    case "spectator":case "spectate":
                        if (world.getSpectator() == null) {
                            player.sendRawMessage(ChatColor.RED + "No spectator location set");
                        } else {
                            player.teleport(world.getSpectator());
                            player.sendRawMessage(ChatColor.GREEN + "Teleported to spectator");
                        }
                        return true;
                }
                player.sendRawMessage(ChatColor.RED + "Usage: /bg " + args[0]
                        + " [lobby/start/lapis/spectator]");
                return true;
                //TODO: Check if locations are set before allowing start
            } else if (args[0].equals("force")) {
                switch (args[1]) {
                    case "lobby":
                        world.setPaused(false);
                        if (world.setPhase(MapInstance.Phase.LOBBY)) {
                            player.sendRawMessage(ChatColor.GREEN + "Set to lobby");
                        }
                        return true;
                    case "start":
                        world.setPaused(false);
                        boolean test = world.setPhase(MapInstance.Phase.LOBBY);
                        if (test) {
                            player.sendRawMessage(ChatColor.GREEN + "Started");
                        }
                        return true;
                    case "stop":
                        world.setPaused(true);
                        if (world.setPhase(MapInstance.Phase.LOBBY)) {
                            player.sendRawMessage(ChatColor.GREEN + "Stopped");
                        }
                        return true;
                    case "block":case "blockPlacing":
                        world.setPaused(false);
                        if (world.setPhase(MapInstance.Phase.BLOCKPLACING)) {
                            player.sendRawMessage(ChatColor.GREEN + "Set to block placing");
                        }
                        return true;
                    case "game":case "gamePlay":
                        world.setPaused(false);
                        if (world.setPhase(MapInstance.Phase.GAMEPLAY)) {
                            player.sendRawMessage(ChatColor.GREEN + "Set to gameplay");
                        }
                        return true;
                }
                player.sendRawMessage(ChatColor.RED + "Usage: /bg force "
                        + "[stop/lobby/block/game]");
                return true;
            }
            //TODO: rules?
            //TODO Change deserialize error mesage for locations to "does not exist"
        } else if (args.length == 1 && args[0].equals("debug") && sender instanceof Player) {
            ((Player) sender).sendRawMessage(maps.get(((Player) sender).getWorld()
                    .getName()).getPlayers().size() + "");
        } else if (args.length == 1 && args[0].equals("registerWorld") && sender instanceof Player) {
            if (maps.containsKey(((Player) sender).getWorld().getName())) {
                ((Player) sender).sendRawMessage(ChatColor.RED + "World already registered");
            } else {
                maps.put(((Player) sender).getWorld().getName(), MapInstance.deserialize(
                        ((Player) sender).getWorld().getName(), parent));
            }
            return true;
        } else if (args.length == 3 && args[0].equals("set") && sender instanceof Player) {
            Player player = (Player) sender;
            MapInstance world = maps.get(player.getWorld().getName());
            int num = 0;
            if (args[1].equals("placementTime")) {
                try {
                    num = Integer.parseInt(args[2]);
                    if (num < 1) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too low (min 1)");
                    } else if (num > 3600) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too large (max 3600)");
                    } else {
                        world.setPlacementRoundTimeLimit(num);
                        player.sendRawMessage(ChatColor.GREEN + "Placement time set");
                    }
                } catch (NumberFormatException e) {
                    player.sendRawMessage(ChatColor.RED + "Specified value "
                            + args[2] + " is not a valid int");
                }
            } else if (args[1].equals("lobbyTime")) {
                try {
                    num = Integer.parseInt(args[2]);
                    if (num < 1) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too low (min 1)");
                    } else if (num > 3600) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too large (max 3600)");
                    } else {
                        world.setLobbyCountdown(num);
                        player.sendRawMessage(ChatColor.GREEN + "Lobby countdown set");
                    }
                } catch (NumberFormatException e) {
                    player.sendRawMessage(ChatColor.RED + "Specified value "
                            + args[2] + " is not a valid int");
                }
            } else if (args[1].equals("gameTime")) {
                try {
                    num = Integer.parseInt(args[2]);
                    if (num < 1) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too low (min 1)");
                    } else if (num > 3600) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too large (max 3600)");
                    } else {
                        world.setGameRoundTimeLimit(num);
                        player.sendRawMessage(ChatColor.GREEN + "Game time set");
                    }
                } catch (NumberFormatException e) {
                    player.sendRawMessage(ChatColor.RED + "Specified value "
                            + args[2] + " is not a valid int");
                }
            } else if (args[1].equals("minStartingPlayers")) {
                try {
                    num = Integer.parseInt(args[2]);
                    if (num < 1) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too low (min 1)");
                    } else if (num > 20) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too large (max 20)");
                    } else {
                        world.setMinStartingPlayers(num);
                        player.sendRawMessage(ChatColor.GREEN + "Min starting players set");
                    }
                } catch (NumberFormatException e) {
                    player.sendRawMessage(ChatColor.RED + "Specified value "
                            + args[2] + " is not a valid int");
                }
            } else if (args[1].equals("basePoints")) {
                try {
                    num = Integer.parseInt(args[2]);
                    if (num < 0) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too low (min 0)");
                    } else if (num > 1000000) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too large (max 1000000)");
                    } else {
                        world.setPointsForFinishing(num);
                        player.sendRawMessage(ChatColor.GREEN + "Base points set");
                    }
                } catch (NumberFormatException e) {
                    player.sendRawMessage(ChatColor.RED + "Specified value "
                            + args[2] + " is not a valid int");
                }
            } else if (args[1].equals("firstPlaceBonus")) {
                try {
                    num = Integer.parseInt(args[2]);
                    if (num < 0) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too low (min 0)");
                    } else if (num > 1000000) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too large (max 1000000)");
                    } else {
                        world.setPointBonusForFirst(num);
                        player.sendRawMessage(ChatColor.GREEN + "First place bonus set");
                    }
                } catch (NumberFormatException e) {
                    player.sendRawMessage(ChatColor.RED + "Specified value "
                            + args[2] + " is not a valid int");
                }
            } else if (args[1].equals("secondPlaceBonus")) {
                try {
                    num = Integer.parseInt(args[2]);
                    if (num < 0) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too low (min 0)");
                    } else if (num > 1000000) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too large (max 1000000)");
                    } else {
                        world.setPointBonusForSecond(num);
                        player.sendRawMessage(ChatColor.GREEN + "Second place bonus set");
                    }
                } catch (NumberFormatException e) {
                    player.sendRawMessage(ChatColor.RED + "Specified value "
                            + args[2] + " is not a valid int");
                }
            } else if (args[1].equals("pointsToWin")) {
                try {
                    num = Integer.parseInt(args[2]);
                    if (num < 1) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too low (min 1)");
                    } else if (num > 10000000) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too large (max 10000000)");
                    } else {
                        world.setPointsNecessaryToWin(num);
                        player.sendRawMessage(ChatColor.GREEN + "Points necessary to win set");
                    }
                } catch (NumberFormatException e) {
                    player.sendRawMessage(ChatColor.RED + "Specified value "
                            + args[2] + " is not a valid int");
                }
            } else if (args[1].equals("maxHealth")) {
                try {
                    num = Integer.parseInt(args[2]);
                    if (num < 1) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too low (min 1)");
                    } else if (num > 100) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too large (max 100)");
                    } else {
                        world.setHealth(num);
                        player.sendRawMessage(ChatColor.GREEN + "Max health set");
                    }
                } catch (NumberFormatException e) {
                    player.sendRawMessage(ChatColor.RED + "Specified value "
                            + args[2] + " is not a valid int");
                }
            } else if (args[1].equals("roundsToReset")) {
                try {
                    num = Integer.parseInt(args[2]);
                    if (num < 1) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too low (min 1)");
                    } else if (num > 10) {
                        player.sendRawMessage(ChatColor.RED + "Specified value "
                                + args[2] + " is too large (max 10)");
                    } else {
                        world.setRoundsToReset(num);
                        player.sendRawMessage(ChatColor.GREEN + "First place bonus set");
                    }
                } catch (NumberFormatException e) {
                    player.sendRawMessage(ChatColor.RED + "Specified value "
                            + args[2] + " is not a valid int");
                }
            }
        }
        return false;
    }
}
