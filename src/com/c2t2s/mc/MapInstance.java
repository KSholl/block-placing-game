package com.c2t2s.mc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapInstance {

    private World world;
    private Main parent;
    private Location start, spectator, lobby, lapis;
    private int placementRoundTimeLimit, lobbyCountdown, gameRoundTimeLimit,
            minStartingPlayers, pointsForFinishing, pointBonusForFirst,
            pointBonusForSecond, pointsNecessaryToWin, timer, health,
            roundsWithoutWinner, roundsToReset;
    private List<Player> players, waitingOn;
    private Phase phase;
    private RandomizedBlockSelectionMenu blockInventory;
    private Map<Location, ArrowSpawnLocation> arrows;
    private Map<Location, Vector> fireballs;
    private Map<Location, List<ArmorStand>> armorStands;
    private ScoreManager scoreManager;
    //TODO: Make command to enable/disable lapis
    private boolean paused, isLapisDisabled;
    private Set<Location> placedBlocks;
    private Map<Location, MaterialData> existingBlocks;

    public MapInstance(World world, Main parent, Location start,
                       Location spectator, Location lobby, Location lapis,
                       int placementRoundTimeLimit, int lobbyCountdown,
                       int gameRoundTimeLimit, int minStartingPlayers,
                       int pointsForFinishing, int pointBonusForFirst,
                       int pointBonusForSecond, int pointsNecessaryToWin,
                       int health, int roundsToReset) {
        this.spectator = spectator;
        this.start = start;
        this.lobby = lobby;
        this.lapis = lapis;
        this.placementRoundTimeLimit = placementRoundTimeLimit;
        this.lobbyCountdown = lobbyCountdown;
        this.gameRoundTimeLimit = gameRoundTimeLimit;
        this.minStartingPlayers = minStartingPlayers;
        this.pointsForFinishing = pointsForFinishing;
        this.pointBonusForFirst = pointBonusForFirst;
        this.pointBonusForSecond = pointBonusForSecond;
        this.pointsNecessaryToWin = pointsNecessaryToWin;
        this.health = health;
        this.parent = parent;
        this.world = world;
        this.roundsToReset = roundsToReset;
        timer = 0;
        roundsWithoutWinner = 0;
        players = new ArrayList<>();
        waitingOn = new ArrayList<>();
        phase = Phase.LOBBY;
        blockInventory = new RandomizedBlockSelectionMenu(parent.getMaps());
        parent.getServer().getPluginManager().registerEvents(blockInventory,
                parent);
        arrows = new HashMap<>();
        fireballs = new HashMap<>();
        armorStands = new HashMap<>();
        placedBlocks = new HashSet<>();
        existingBlocks = new HashMap<>();
        scoreManager = new ScoreManager(players, waitingOn, pointsForFinishing,
                pointBonusForFirst, pointBonusForSecond, pointsNecessaryToWin,
                this);
    }

    @SuppressWarnings("unchecked")
    public static MapInstance deserialize(String worldName, Main parent) {
        World world = parent.getServer().getWorld(worldName);
        Logger logger = parent.getLogger();
        if (world == null) {
            logger.log(Level.WARNING, "World " + worldName
                    + " did not exist at deserialization. Creating now.");
            world = parent.getServer().createWorld(new WorldCreator(worldName));
        }
//        File worldFile = new File(parent.getDataFolder(), worldName
//                + "Data.yml");
//        if (!worldFile.exists()) {
//            worldFile.getParentFile().mkdirs();
//            logger.log(Level.WARNING, "Config for " + worldName
//                    + " did not exist at deserialization. Creating now.");
//            parent.saveResource(worldName + "Data.yml", false);
//        }
//        FileConfiguration config = new YamlConfiguration();
//        try {
//            config.load(worldFile);
//        } catch (IOException e) {
//            logger.log(Level.WARNING, "Could not read config for " + worldName
//                    + ". Aborting deserialization.");
//            return null;
//        } catch (InvalidConfigurationException e) {
//            logger.log(Level.WARNING, "Config for " + worldName
//                    + " was invalid. Aborting deserialization.");
//            return null;
//        }
        FileConfiguration config = parent.getConfig();
        Location start = null;
        if (!config.contains(worldName + "startingLocation")
                || config.get(worldName + "startingLocation") == null
                || config.get(worldName + "startingLocation").equals("")) {
            logger.log(Level.WARNING, "Start location for world "
                    + worldName + " did not exist in config");
        } else {
            try {
                start = Location.deserialize(((MemorySection) config
                        .get(worldName + "startingLocation")).getValues(false));
            } catch (ClassCastException e) {
                logger.log(Level.WARNING, "Start location for " +
                        "world " + worldName + " was not of proper type: Not a "
                        + "MemorySection");
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "IllegalArgumentException"
                        + " thrown when deserializing start location for "
                        + worldName);
            }
        }
        Location spectator = null;
        if (!config.contains(worldName + "spectatorLocation")
                || config.get(worldName + "spectatorLocation") == null
                || config.get(worldName + "spectatorLocation").equals("")) {
            logger.log(Level.WARNING, "Spectator location for world"
                    + " " + worldName + " did not exist in config");
        } else {
            try {
                spectator = Location.deserialize(((MemorySection) config
                        .get(worldName + "spectatorLocation")).getValues(false));
            } catch (ClassCastException e) {
                logger.log(Level.WARNING, "Spectator location for" +
                        " world " + worldName + " was not of proper type: Not a"
                        + " Map<String, Object>");
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "IllegalArgumentException"
                        + " thrown when deserializing spectator location for "
                        + worldName);
            }
        }
        Location lobby = null;
        if (!config.contains(worldName + "lobbyLocation")
                || config.get(worldName + "lobbyLocation") == null
                || config.get(worldName + "lobbyLocation").equals("")) {
            logger.log(Level.WARNING, "Lobby location for world "
                    + worldName + " did not exist in config");
        } else {
            try {
                lobby = Location.deserialize(((MemorySection) config
                        .get(worldName + "lobbyLocation")).getValues(false));
            } catch (ClassCastException e) {
                logger.log(Level.WARNING, "Lobby location for" +
                        " world " + worldName + " was not of proper type: Not a"
                        + " Map<String, Object>");
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "IllegalArgumentException"
                        + " thrown when deserializing lobby location for "
                        + worldName);
            }
        }
        Location lapis = null;
        if (!config.contains(worldName + "lapisLocation")
                || config.get(worldName + "lapisLocation") == null
                || config.get(worldName + "lapisLocation").equals("")) {
            logger.log(Level.WARNING, "Lapis location for world "
                    + worldName + " did not exist in config");
        } else {
            try {
                lapis = Location.deserialize(((MemorySection) config
                        .get(worldName + "lapisLocation")).getValues(false));
            } catch (ClassCastException e) {
                logger.log(Level.WARNING, "Lapis location for" +
                        " world " + worldName + " was not of proper type: Not a"
                        + " Map<String, Object>");
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "IllegalArgumentException"
                        + " thrown when deserializing lapis location for "
                        + worldName);
            }
        }
        int placementRoundTimeLimit = 45;
        if (!config.contains(worldName + "placementRoundTimeLimit")) {
            logger.log(Level.WARNING, "placementRoundTimeLimit for "
                    + "world " + worldName + " did not exist in config");
        } else {
            placementRoundTimeLimit = config.getInt(worldName
                    + "placementRoundTimeLimit");
            if (placementRoundTimeLimit < 1) {
                logger.log(Level.WARNING, "placementRoundTimeLimit from config"
                        + " was too low for world " + worldName + ": "
                        + placementRoundTimeLimit);
                placementRoundTimeLimit = 45;
            } else if (placementRoundTimeLimit > 3600) {
                logger.log(Level.WARNING, "placementRoundTimeLimit from config"
                        + " was too high for world " + worldName + ": "
                        + placementRoundTimeLimit);
                placementRoundTimeLimit = 45;
            }
        }
        int lobbyCountdown = 30;
        if (!config.contains(worldName + "lobbyCountdown")) {
            logger.log(Level.WARNING, "lobbyCountdown for "
                    + "world " + worldName + " did not exist in config");
        } else {
            lobbyCountdown = config.getInt(worldName
                    + "lobbyCountdown");
            if (lobbyCountdown < 1) {
                logger.log(Level.WARNING, "lobbyCountdown from config"
                        + " was too low for world " + worldName + ": "
                        + lobbyCountdown);
                lobbyCountdown = 30;
            } else if (lobbyCountdown > 3600) {
                logger.log(Level.WARNING, "lobbyCountdown from config"
                        + " was too high for world " + worldName + ": "
                        + lobbyCountdown);
                lobbyCountdown = 3600;
            }
        }
        int gameRoundTimeLimit = 300;
        if (!config.contains(worldName + "gameRoundTimeLimit")) {
            logger.log(Level.WARNING, "gameRoundTimeLimit for "
                    + "world " + worldName + " did not exist in config");
        } else {
            gameRoundTimeLimit = config.getInt(worldName
                    + "gameRoundTimeLimit");
            if (gameRoundTimeLimit < 1) {
                logger.log(Level.WARNING, "gameRoundTimeLimit from config"
                        + " was too low for world " + worldName + ": "
                        + gameRoundTimeLimit);
                gameRoundTimeLimit = 300;
            } else if (gameRoundTimeLimit > 3600) {
                logger.log(Level.WARNING, "gameRoundTimeLimit from config"
                        + " was too high for world " + worldName + ": "
                        + gameRoundTimeLimit);
                gameRoundTimeLimit = 3600;
            }
        }
        int minStartingPlayers = 1;
        if (!config.contains(worldName + "minStartingPlayers")) {
            logger.log(Level.WARNING, "minStartingPlayers for "
                    + "world " + worldName + " did not exist in config");
        } else {
            minStartingPlayers = config.getInt(worldName
                    + "minStartingPlayers");
            if (minStartingPlayers < 1) {
                logger.log(Level.WARNING, "minStartingPlayers from config"
                        + " was too low for world " + worldName + ": "
                        + minStartingPlayers);
                minStartingPlayers = 1;
            } else if (minStartingPlayers > 20) {
                logger.log(Level.WARNING, "minStartingPlayers from config"
                        + " was too high for world " + worldName + ": "
                        + minStartingPlayers);
                minStartingPlayers = 20;
            }
        }
        int pointsForFinishing = 2;
        if (!config.contains(worldName + "pointsForFinishing")) {
            logger.log(Level.WARNING, "pointsForFinishing for "
                    + "world " + worldName + " did not exist in config");
        } else {
            pointsForFinishing = config.getInt(worldName
                    + "pointsForFinishing");
            if (pointsForFinishing < 0) {
                logger.log(Level.WARNING, "pointsForFinishing from config"
                        + " was too low for world " + worldName + ": "
                        + pointsForFinishing);
                pointsForFinishing = 0;
            } else if (pointsForFinishing > 1000000) {
                logger.log(Level.WARNING, "pointsForFinishing from config"
                        + " was too high for world " + worldName + ": "
                        + pointsForFinishing);
                pointsForFinishing = 1000000;
            }
        }
        int pointBonusForFirst = 2;
        if (!config.contains(worldName + "pointBonusForFirst")) {
            logger.log(Level.WARNING, "pointBonusForFirst for "
                    + "world " + worldName + " did not exist in config");
        } else {
            pointBonusForFirst = config.getInt(worldName
                    + "pointBonusForFirst");
            if (pointBonusForFirst < 0) {
                logger.log(Level.WARNING, "pointBonusForFirst from config"
                        + " was too low for world " + worldName + ": "
                        + pointBonusForFirst);
                pointBonusForFirst = 0;
            } else if (pointBonusForFirst > 1000000) {
                logger.log(Level.WARNING, "pointBonusForFirst from config"
                        + " was too high for world " + worldName + ": "
                        + pointBonusForFirst);
                pointBonusForFirst = 1000000;
            }
        }
        int pointBonusForSecond = 1;
        if (!config.contains(worldName + "pointBonusForSecond")) {
            logger.log(Level.WARNING, "pointBonusForSecond for "
                    + "world " + worldName + " did not exist in config");
        } else {
            pointBonusForSecond = config.getInt(worldName
                    + "pointBonusForSecond");
            if (pointBonusForSecond < 0) {
                logger.log(Level.WARNING, "pointBonusForSecond from config"
                        + " was too low for world " + worldName + ": "
                        + pointBonusForSecond);
                pointBonusForSecond = 0;
            } else if (pointBonusForSecond > 1000000) {
                logger.log(Level.WARNING, "pointBonusForSecond from config"
                        + " was too high for world " + worldName + ": "
                        + pointBonusForSecond);
                pointBonusForSecond = 1000000;
            }
            if (pointBonusForSecond > pointBonusForFirst) {
                logger.log(Level.WARNING, "pointBonusForSecond("
                        + pointBonusForSecond + ") from config"
                        + " was higher than pointBonusForFirst ("
                        + pointBonusForFirst + ") for world "
                        + worldName);
                pointBonusForSecond = pointBonusForFirst;
            }
        }
        int pointsNecessaryToWin = 20;
        if (!config.contains(worldName + "pointsNecessaryToWin")) {
            logger.log(Level.WARNING, "pointsNecessaryToWin for "
                    + "world " + worldName + " did not exist in config");
        } else {
            pointsNecessaryToWin = config.getInt(worldName
                    + "pointsNecessaryToWin");
            if (pointsNecessaryToWin < 1) {
                logger.log(Level.WARNING, "pointsNecessaryToWin from config"
                        + " was too low for world " + worldName + ": "
                        + pointsNecessaryToWin);
                pointsNecessaryToWin = 1;
            } else if (pointsNecessaryToWin > 10000000) {
                logger.log(Level.WARNING, "pointsNecessaryToWin from config"
                        + " was too high for world " + worldName + ": "
                        + pointsNecessaryToWin);
                pointsNecessaryToWin = 10000000;
            }
        }
        int health = 20;
        if (!config.contains(worldName + "health")) {
            logger.log(Level.WARNING, "health for " + "world " + worldName
                    + " did not exist in config");
        } else {
            health = config.getInt(worldName + "health");
            if (health < 1) {
                logger.log(Level.WARNING, "health from config"
                        + " was too low for world " + worldName + ": "
                        + health);
                health = 1;
            } else if (health > 100) {
                logger.log(Level.WARNING, "health from config"
                        + " was too high for world " + worldName + ": "
                        + health);
                health = 100;
            }
        }
        int roundsToReset = 3;
        if (!config.contains(worldName + "roundsToReset")) {
            logger.log(Level.WARNING, "roundsToReset for world " + worldName
                    + " did not exist in config");
        } else {
            roundsToReset = config.getInt(worldName + "roundsToReset");
            if (roundsToReset < 1) {
                logger.log(Level.WARNING, "roundsToReset from config was too low for world "
                        + worldName + ": " + roundsToReset);
                roundsToReset = 1;
            } else if (roundsToReset > 10) {
                logger.log(Level.WARNING, "roundsToReset from config was too high for world "
                        + worldName + ": " + roundsToReset);
                roundsToReset = 10;
            }
        }
        return new MapInstance(world, parent, start, spectator, lobby, lapis,
                placementRoundTimeLimit, lobbyCountdown, gameRoundTimeLimit,
                minStartingPlayers, pointsForFinishing, pointBonusForFirst,
                pointBonusForSecond, pointsNecessaryToWin, health, roundsToReset);
    }

    public void serialize() {
        String name = world.getName();
//        File worldFile = new File(parent.getDataFolder(), name + "Data.yml");
//        if (!worldFile.exists()) {
//            worldFile.getParentFile().mkdirs();
//            parent.getLogger().log(Level.WARNING, "Config for " + name
//                    + " did not exist at serialization. Creating now.");
//            parent.saveResource(world.getName() + "Data.yml", false);
//        }
//        FileConfiguration config = new YamlConfiguration();
//        try {
//            config.load(worldFile);
//        } catch (IOException e) {
//            parent.getLogger().log(Level.WARNING, "Could not read config for "
//                    + name + ". Aborting serialization.");
//            return;
//        } catch (InvalidConfigurationException e) {
//            parent.getLogger().log(Level.WARNING, "Config for " + name
//                    + " was invalid. Aborting serialization.");
//            return;
//        }
        FileConfiguration config = parent.getConfig();
        config.set(name + "placementRoundTimeLimit",
                placementRoundTimeLimit);
        config.set(name + "lobbyCountdown", lobbyCountdown);
        config.set(name + "minStartingPlayers", minStartingPlayers);
        config.set(name + "gameRoundTimeLimit", gameRoundTimeLimit);
        config.set(name + "pointsForFinishing", pointsForFinishing);
        config.set(name + "pointBonusForFirst", pointBonusForFirst);
        config.set(name + "pointBonusForSecond", pointBonusForSecond);
        config.set(name + "pointsNecessaryToWin", pointsNecessaryToWin);
        config.set(name + "health", health);
        config.set(name + "roundsToReset", roundsToReset);
        if (start == null) {
            config.set(name + "startingLocation", "");
        } else {
            config.set(name + "startingLocation", start.serialize());
            parent.saveConfig();
        }
        if (lapis == null) {
            config.set(name + "lapisLocation", "");
        } else {
            config.set(name + "lapisLocation", lapis.serialize());
        }
        if (spectator == null) {
            config.set(name + "spectatorLocation", "");
        } else {
            config.set(name + "spectatorLocation", spectator
                    .serialize());
        }
        if (lobby == null) {
            config.set(name + "lobbyLocation", "");
        } else {
            config.set(name + "lobbyLocation", lobby.serialize());
        }
        parent.saveConfig();
    }

    public boolean isStartSet() {
        return !(start == null);
    }

    public boolean isSpectatorSet() {
        return !(spectator == null);
    }

    public boolean isLobbySet() {
        return !(lobby == null);
    }

    public boolean isLapisSet() {
        return !(lapis == null);
    }

    public Location getStart() {
        return start;
    }

    public Location getSpectator() {
        return spectator;
    }

    public Location getLobby() {
        return lobby;
    }

    public Location getLapis() {
        return lapis;
    }

    public void setPlacementRoundTimeLimit(int placementRoundTimeLimit) {
        this.placementRoundTimeLimit = placementRoundTimeLimit;
    }

    public void setLobbyCountdown(int lobbyCountdown) {
        this.lobbyCountdown = lobbyCountdown;
    }

    public void setGameRoundTimeLimit(int gameRoundTimeLimit) {
        this.gameRoundTimeLimit = gameRoundTimeLimit;
    }

    public void setMinStartingPlayers(int minStartingPlayers) {
        this.minStartingPlayers = minStartingPlayers;
    }

    public void setPointsForFinishing(int pointsForFinishing) {
        this.pointsForFinishing = pointsForFinishing;
    }

    public void setPointBonusForFirst(int pointBonusForFirst) {
        this.pointBonusForFirst = pointBonusForFirst;
    }

    public void setPointBonusForSecond(int pointBonusForSecond) {
        this.pointBonusForSecond = pointBonusForSecond;
    }

    public void setPointsNecessaryToWin(int pointsNecessaryToWin) {
        this.pointsNecessaryToWin = pointsNecessaryToWin;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setRoundsToReset(int roundsToReset) {
        this.roundsToReset = roundsToReset;
    }

    public int getPlacementRoundTimeLimit() {
        return placementRoundTimeLimit;
    }

    public int getLobbyCountdown() {
        return lobbyCountdown;
    }

    public int getGameRoundTimeLimit() {
        return gameRoundTimeLimit;
    }

    public int getMinStartingPlayers() {
        return minStartingPlayers;
    }

    public int getPointsForFinishing() {
        return pointsForFinishing;
    }

    public int getPointBonusForFirst() {
        return pointBonusForFirst;
    }

    public int getPointBonusForSecond() {
        return pointBonusForSecond;
    }

    public int getPointsNecessaryToWin() {
        return pointsNecessaryToWin;
    }

    public World getWorld() {
        return world;
    }

    public JavaPlugin getParent() {
        return parent;
    }

    public Phase getPhase() {
        return phase;
    }

    public boolean isLapisDisabled() {
        return isLapisDisabled;
    }

    public void setLapisDisabled(boolean isLapisDisabled) {
        this.isLapisDisabled = isLapisDisabled;
    }

    public Set<Location> getPlacedBlocks() {
        return placedBlocks;
    }

    public Map<Location, MaterialData> getExistingBlocks() {
        return existingBlocks;
    }

    public Map<Location, ArrowSpawnLocation> getArrows() {
        return arrows;
    }

    public Map<Location, Vector> getFireballs() {
        return fireballs;
    }

    public Map<Location, List<ArmorStand>> getArmorStands() {
        return armorStands;
    }

    public List<Player> getWaitingOn() {
        return waitingOn;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public boolean setPhase(final Phase phase) {
        String locationCheck = checkLocationsSet();
        if (!(locationCheck == null) && !paused) {
            this.phase = Phase.LOBBY;
            paused = true;
            parent.getLogger().log(Level.WARNING, locationCheck + " locations "
                    + "for " + world.getName() + " were null, forcing paused "
                    + "lobby");
            warnOPs(ChatColor.RED + "ERROR" + ChatColor.RESET + ": "
                    + locationCheck + " not set, forcing paused lobby");
            return false;
        }
        clearPotionEffects();
        healPlayers();
        switch (phase) {
            case LOBBY:
                if (!paused && players.size() >= minStartingPlayers) {
                    setPhase(Phase.LOBBYCOUNTDOWN);
                } else if (paused) {
                    //[CHAT] Game welcome when paused
                    broadcastMessage("Welcome to " + ChatColor.GOLD
                            + "[Block Placing Game]" + ChatColor.RESET + ".");
                } else {
                    this.phase = Phase.LOBBY;
                    setFlight(false);
                    //[CHAT] Game starting message
                    broadcastMessage("Welcome to " + ChatColor.GOLD
                            + "[Block Placing Game]" + ChatColor.RESET
                            + ". Game will start when there are "
                            + minStartingPlayers + " players.");
                    TPAllPlayers(lobby);
                    clearPlayerInventories();
                    forceGamemodeTwo();
                    scoreManager.clearScoreboard();
                    scoreManager.setScoreboardVisible(false);
                    resetBlocks();
                }
                break;
            case LOBBYCOUNTDOWN:
                if (players.size() < minStartingPlayers) {
                    setPhase(Phase.LOBBY);
                } else {
                    if (!this.phase.equals(Phase.LOBBY)) {
                        TPAllPlayers(lobby);
                        clearPlayerInventories();
                        forceGamemodeTwo();
                    }
                    this.phase = Phase.LOBBYCOUNTDOWN;
                    setFlight(false);
                    //[CHAT] 'Game starting in' message
                    broadcastMessage("Game starting in " + ChatColor.RED
                            + timeDisplay(lobbyCountdown));
                    timer = lobbyCountdown - 1;
                    scoreManager.clearScoreboard();
                    scoreManager.setScoreboardVisible(false);
                    resetBlocks();
                    lobbyCountdown();
                }
                break;
            case BLOCKPLACING:
                this.phase = phase;
                //[CHAT] Block placing introduction
                broadcastMessage(ChatColor.GREEN + "Block Placing Round"
                        + ChatColor.RESET + ": You have " + ChatColor.RED
                        + timeDisplay(placementRoundTimeLimit) + ChatColor.RESET
                        + " to select a block and place it somewhere on the "
                        + "track");
                timer = placementRoundTimeLimit - 1;
                TPAllPlayers(start);
                setFlight(true);
                blockInventory.generateInventory(players.size() + 1, true);
                blockInventory.openInventory(players);
                waitingOn.clear();
                waitingOn.addAll(players);
                scoreManager.setScoreboardVisible(true);
                blockPlacingCountdown();
                break;
            case GAMEPLAY:
                this.phase = phase;
                setFlight(false);
                TPAllPlayers(start);
                clearPlayerInventories();
                waitingOn.clear();
                waitingOn.addAll(players);
                //[CHAT] Gameplay round introduction
                broadcastMessage("You have " + ChatColor.RED
                        + timeDisplay(gameRoundTimeLimit) + ChatColor.RESET
                        + " to get to the end of the course! Be quick!");
                timer = gameRoundTimeLimit - 1;
                scoreManager.setScoreboardVisible(true);
                playRoundTimer();
                break;
            case SUDDENDEATH:
                this.phase = phase;
                setFlight(false);
                TPNotWaitingOn(spectator);
                TPWaitingOn(start);
                //[CHAT] Sudden Death introduction
                broadcastMessage(ChatColor.RED + "Sudden Death! Get to the end of the course!");
                break;
            //LOBBYPENDING, GAMEPLAYPENDING, BLOCKPENDING
            case LOBBYPENDING:
                this.phase = phase;
                lobbyPending(100L);
                break;
            case GAMEPLAYPENDING:
                this.phase = phase;
                gameplayPending(40L);
                break;
            case BLOCKPENDING:
                this.phase = phase;
                blockPending(100L);
                break;
            case DEATHMATCHPENDING:
                this.phase = phase;
                deathMatchPending(60L);
                break;
        }
        return true;
    }

    public void lobbyPending(long time) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(parent, new Runnable() {
            @Override
            public void run() {
                System.out.print("Huh");
                if (phase.equals(Phase.LOBBYPENDING)) {
                    setPhase(Phase.LOBBY);
                }
            }
        }, time);
    }

    public void gameplayPending(long time) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(parent, new Runnable() {
            @Override
            public void run() {
                if (phase.equals(Phase.GAMEPLAYPENDING)) {
                    setPhase(Phase.GAMEPLAY);
                }
            }
        }, time);
    }

    public void blockPending(long time) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(parent, new Runnable() {
            @Override
            public void run() {
                if (phase.equals(Phase.BLOCKPENDING)) {
                    setPhase(Phase.BLOCKPLACING);
                }
            }
        }, time);
    }

    public void deathMatchPending(long time) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(parent, new Runnable() {
            @Override
            public void run() {
                if (phase.equals(Phase.DEATHMATCHPENDING)) {
                    setPhase(Phase.SUDDENDEATH);
                }
            }
        }, time);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (!phase.equals(Phase.LOBBY) && paused) {
            setPhase(Phase.LOBBY);
        }
    }

    public void setStart(Location start) {
        this.start = start;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public void setLapis(Location lapis) {
        this.lapis = lapis;
    }

    public void setSpectator(Location spectator) {
        this.spectator = spectator;
    }

    public void addPlayer(Player player) {
        players.add(player);
        healPlayer(player);
        player.setGameMode(GameMode.ADVENTURE);
        scoreManager.add(player);
        //[CHAT] Welcome message
        broadcastMessage(ChatColor.YELLOW + player.getName() + " joined");
        player.sendRawMessage(ChatColor.YELLOW + player.getName() + " joined");
        if (phase == Phase.LOBBY || phase == Phase.LOBBYCOUNTDOWN) {
            if (lobby != null) {
                player.teleport(lobby);
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(parent, new Runnable() {
                @Override
                public void run() {
                    if (phase == Phase.LOBBY && !paused && players.size() >= minStartingPlayers) {
                        setPhase(Phase.LOBBYCOUNTDOWN);
                    }
                }
            }, 1L);
        } else {
            if (spectator != null) {
                player.teleport(spectator);
                //[CHAT] Message when joining in the middle of a game
                player.sendRawMessage("Game in progess. You will be added next "
                        + "round.");
            }
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);
        waitingOn.remove(player);
        player.getInventory().clear();
        healPlayer(player);
    }

    public enum Phase {
        LOBBY, LOBBYCOUNTDOWN, BLOCKPLACING, GAMEPLAY, SUDDENDEATH, LOBBYPENDING, GAMEPLAYPENDING, BLOCKPENDING, DEATHMATCHPENDING
    }

    private void TPAllPlayers(Location location) {
        if (location != null) {
            for (Player p : players) {
                p.teleport(location);
            }
        }
    }

    private void TPWaitingOn(Location location) {
        if (location != null) {
            for (Player p : players) {
                p.teleport(location);
            }
        }
    }

    private void TPNotWaitingOn(Location location) {
        if (location != null) {
            List<Player> notWaitingOn = new ArrayList<>(players);
            notWaitingOn.removeAll(waitingOn);
            for (Player p : notWaitingOn) {
                p.teleport(location);
            }
        }
    }

    private void setFlight(boolean value) {
        if (parent.getServer().getAllowFlight()) {
            if (value) {
                for (Player p : players) {
                    p.setAllowFlight(true);
                    p.setFlying(true);
                }
            } else {
                for (Player p : players) {
                    p.setAllowFlight(false);
                }
            }
        }
    }

    public void healPlayers() {
        for (Player p : players) {
            if (health > p.getMaxHealth()) {
                p.setMaxHealth(health);
            }
            p.setHealth(health);
        }
    }

    public void healPlayer(Player p) {
        if (health != p.getMaxHealth()) {
            p.setMaxHealth(health);
        }
        p.setHealth(health);
    }

    private void clearPlayerInventories() {
        for (Player p : players) {
            p.getInventory().clear();
        }
    }

    private void forceGamemodeTwo() {
        for (Player p : players) {
            p.setGameMode(GameMode.ADVENTURE);
        }
    }

    public void clearPotionEffects() {
        for (Player player : players) {
            for (PotionEffect p : player.getActivePotionEffects()) {
                player.removePotionEffect(p.getType());
            }
            player.setFireTicks(0);
        }
    }

    public void clearPotionEffect(Player player) {
        for (PotionEffect p : player.getActivePotionEffects()) {
            player.removePotionEffect(p.getType());
        }
        player.setFireTicks(0);
    }

    private void lobbyCountdown() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(parent, new Runnable() {
            public void run() {
                if (timer == 0) {
                    setPhase(Phase.BLOCKPLACING);
                } else if (players.size() < 2 && players.size()
                        < minStartingPlayers) {
                    if (players.size() == 1) {
                        //[CHAT] Last player alive message
                        players.get(0).sendRawMessage(ChatColor.GOLD +
                                "You were the last player left, so I guess you won!");
                    }
                    setPhase(Phase.LOBBY);
                } else if (phase.equals(Phase.LOBBYCOUNTDOWN)) {
                    if (timer == 5 || timer == 15 || timer == 30) {
                        //[CHAT] Parsed countdown timer
                        broadcastMessage("Game starting in " + timeDisplay(timer));
                    } else if (timer % 60 == 0) {
                        //[CHAT] Parsed countdown timer
                        broadcastMessage("Game starting in " + timeDisplay(timer));
                    }
                    timer--;
                    lobbyCountdown();
                }
            }
        }, 20L);
    }

    private void blockPlacingCountdown() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(parent, new Runnable() {
            public void run() {
                if (timer == 0) {
                    setPhase(Phase.GAMEPLAY);
                } else if (players.size() < 2 && players.size()
                        < minStartingPlayers) {
                    if (players.size() == 1) {
                        //[CHAT] Last player alive message
                        players.get(0).sendRawMessage(ChatColor.GOLD +
                                "You were the last player left, so I guess you won!");
                    }
                    setPhase(Phase.LOBBY);
                } else if (waitingOn.size() == 0) {
                    //[CHAT] All blocks placed message
                    broadcastMessage(ChatColor.GREEN + "All blocks placed. Starting round.");
                    setPhase(Phase.GAMEPLAYPENDING);
                } else if (phase.equals(Phase.BLOCKPLACING)) {
                    if (timer == 5 || timer == 15 || timer == 30) {
                        //[CHAT] Parsed countdown timer
                        broadcastMessage(timeDisplay(timer) + " left");
                    } else if (timer % 60 == 0) {
                        //[CHAT] Parsed countdown timer
                        broadcastMessage(timeDisplay(timer) + " left");
                    }
                    timer--;
                    blockPlacingCountdown();
                }
            }
        }, 20L);
    }

    private void playRoundTimer() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(parent, new Runnable() {
            public void run() {
                if (timer == 0) {
                    for (Player p : waitingOn) {
                        //[CHAT] Message players that time is up
                        p.sendRawMessage(ChatColor.RED + "Time's up!");
                    }
                    TPWaitingOn(spectator);
                    parent.getServer().broadcastMessage(scoreManager.roundEnd());
                } else if (players.size() < 2 && players.size()
                        < minStartingPlayers) {
                    if (players.size() == 1) {
                        //[CHAT] Last player alive message
                        players.get(0).sendRawMessage(ChatColor.GOLD +
                                "You were the last player left, so I guess you won!");
                    }
                    setPhase(Phase.LOBBY);
                } else if (waitingOn.size() == 0) {
                    parent.getServer().broadcastMessage(scoreManager.roundEnd());
                } else if (phase.equals(Phase.GAMEPLAY)) {
                    if (timer == 5 || timer == 15 || timer == 30) {
                        //[CHAT] Parsed countdown timer
                        broadcastMessage(timeDisplay(timer) + " left");
                    } else if (timer % 60 == 0) {
                        //[CHAT] Parsed countdown timer
                        broadcastMessage(timeDisplay(timer) + " left");
                    }
                    timer--;
                    //Makes sure players receive effects even when standing still on a block
                    for (Player p : players) {
                        PlayerMoveEvent e = new PlayerMoveEvent(p, p.getLocation(),
                                p.getLocation().add(1, 0, 0));
                        e.setCancelled(true);
                        parent.getServer().getPluginManager().callEvent(e);
                    }
                    for (ArrowSpawnLocation location : arrows.values()) {
                        Arrow arrowCopy = world.spawnArrow(location.getArrowLocation(),
                                location.getArrowDirection(), 1f, 12);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(parent,
                                new Runnable() {
                                    public void run() {
                                        if (!arrowCopy.isDead()) {
                                            arrowCopy.remove();
                                        }
                                    }
                                }, 60L);
                    }
                    for (Location location: fireballs.keySet()) {
                        Fireball fireball = (Fireball) world.spawnEntity(location, EntityType.FIREBALL);
                        fireball.setDirection(fireballs.get(location));
                        Bukkit.getScheduler().scheduleSyncDelayedTask(parent,
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (!fireball.isDead()) {
                                        fireball.remove();
                                    }
                                }
                            }, 60L);
                    }
                    playRoundTimer();
                }
            }
        }, 20L);
    }

    private String timeDisplay(int seconds) {
        if (seconds > 119) {
            if (seconds % 60 == 0) {
                return seconds / 60 + " minutes";
            } else if (seconds % 60 == 1) {
                return seconds / 60 + " minutes and 1 second";
            } else {
                return seconds / 60 + " minutes and " + (seconds - seconds / 60
                        * 60) + " seconds";
            }
        } else if (seconds > 60) {
            if (seconds % 60 == 1) {
                return "1 minute and 1 second";
            } else {
                return "1 minute and " + (seconds - 60) + " seconds";
            }
        } else if (seconds == 60) {
            return "1 minute";
        } else if (seconds == 1) {
            return "1 second";
        } else {
            return seconds + " seconds";
        }
    }

    public void broadcastMessage(String message) {
        for (Player p : world.getPlayers()) {
            p.sendRawMessage(message);
        }
    }

    public void warnOPs(String message) {
        for (Player p : world.getPlayers()) {
            if (p.isOp()) {
                p.sendRawMessage(message);
            }
        }
    }

    private String checkLocationsSet() {
        if (start != null && spectator != null && lobby != null && lapis
                != null) {
            return null;
        }
        String invalidLocations = "";
        if (start == null) {
            invalidLocations += "start, ";
        }
        if (spectator == null) {
            invalidLocations += "spectator, ";
        }
        if (lobby == null) {
            invalidLocations += "lobby, ";
        }
        if (lapis == null) {
            invalidLocations += "lapis, ";
        }
        if (invalidLocations.length() > 0) {
            invalidLocations = invalidLocations.substring(0,
                    invalidLocations.length() - 2);
        }
        return invalidLocations;
    }

    @SuppressWarnings("deprecation")
    public void resetBlocks() {
        for (Location location : placedBlocks) {
            if (world.getBlockAt(location)
                    .getType().equals(Material.DISPENSER)) {
                world.getBlockAt(location).setType(Material.AIR);
            } else {
                world.getBlockAt(location).setType(Material.STONE);
            }
        }
        placedBlocks.clear();
        for (Location location : existingBlocks.keySet()) {
            world.getBlockAt(location).setType(existingBlocks.get(location)
                    .getItemType());
            world.getBlockAt(location).setData(existingBlocks.get(location)
                    .getData());
        }
        existingBlocks.clear();
        arrows.clear();
        fireballs.clear();
        for (List<ArmorStand> stands: armorStands.values()) {
            for (ArmorStand armorStand: stands) {
                if (!armorStand.isDead()) {
                    armorStand.remove();
                }
            }
        }
        armorStands.clear();
        //TODO: Make this clear any new collections (fireballs, ghost blocks)
    }

    public void stop() {
        for (Player p : players) {
            p.getInventory().clear();
            if (spectator != null) {
                p.teleport(spectator);
            }
        }
        healPlayers();
        serialize();
    }

    public void roundWithoutWinner() {
        if (roundsWithoutWinner < 2) {
            roundsWithoutWinner++;
            //[CHAT] Telling how many rounds til wipe
            broadcastMessage(ChatColor.RED + "If nobody finishes for "
                    + (roundsToReset + 1 - roundsWithoutWinner)
                    + " rounds, the course will be cleaned up");
        } else {
            //[CHAT] No Winner for 3 rounds
            broadcastMessage(ChatColor.RED + "No winner in last " + roundsToReset
                    + " rounds. Cleaning up course...");
            resetBlocks();
            roundsWithoutWinner = 0;
        }
    }
}

//json config