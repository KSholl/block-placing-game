package com.c2t2s.mc;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    //TODO: Handle calls to maps that could have null MapInstances
    private Map<String, MapInstance> maps;
    private FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        config.options().copyDefaults(true);
        config.addDefault("bgGameWorlds", "7k9u");
        config.addDefault("cancelDamageInNonGameWorlds", false);
        config.addDefault("infiniteSprintInNonGameWorlds", false);
        saveConfig();
        maps = new HashMap<>();
        for (String worldName: config.getString("bgGameWorlds").split(",")) {
            MapInstance data;
            if (!worldName.trim().equals("")) {
                data = MapInstance.deserialize(worldName.trim(), this);
                if (data != null) {
                    maps.put(worldName, data);
                }
            }
        }
        getServer().getPluginManager().registerEvents(new DamageAlterer(config
                .getBoolean("cancelDamageInNonGameWorlds"), maps), this);
        getServer().getPluginManager().registerEvents(
                new BlockPlacementListener(maps), this);
        getServer().getPluginManager().registerEvents(new LapisTeleporter(maps),
                this);
        getServer().getPluginManager().registerEvents(new InfiniteSprint(config
                .getBoolean("infiniteSprintInNonGameWorlds"), maps), this);
        getServer().getPluginManager().registerEvents(new PotionEffectGiver(
                maps), this);
        getServer().getPluginManager().registerEvents(new LockedDispensers(),
                this);
        getServer().getPluginManager().registerEvents(
                new PlayerCountUpdater(maps), this);
        CommandHandler handler = new CommandHandler(this, maps);
        getCommand("blockGame").setExecutor(handler);
        getCommand("bg").setExecutor(handler);
        autosave();
    }

    @Override
    public void onDisable() {
        for (MapInstance map: maps.values()) {
            if (map != null) {
                map.stop();
            }
        }
    }

    public Map<String, MapInstance> getMaps() {
        return maps;
    }

    public void autosave() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                for (MapInstance map: maps.values()) {
                    getLogger().log(Level.INFO, "Autosaving...");
                    if (map != null) {
                        map.serialize();
                    }
                }
                autosave();
            }
        }, 6000L);
    }

    //Assumptions mad:
    //There is a hub world that players are sent to and other worlds with 1 match each on them
    //Or there is one world running the game
    //Players can't fly away during block placing
    //Players die when leaving course
    //No harming blocks availible to spectators


    //Get Help for:
    //[ ]Chat Overhaul
    //[ ]Texture pack

    //Features to add:
    //[/] Commands to set numeric things
    //[ ] Make Invis removable
    //[ ] /bg force stop displays two welcomes and doesn't stop it
    //[/] Fix cleanup timer
    //[X] Build game on scope of world
    //[X] Autosave world data
    //[X] Wipe course if no completion for 3 rounds
    //[X] Pending Phases
    //[X] Tp blocks changed to velocity apply
    //[ ] Speedrunners style timer?
    //[ ] Countdown & delay for starting rounds
    //[ ] Staggered result messages
    //[X] Take a look at death messages - one for arrow kills?
    //[ ] Noise played when incorrectly placing
    //[ ] Noise played when lapis tp

    //Ideas:
    //Customizable block spawnrates
    //Customizable health values
    //Format timer output as boss bar

    //Block Ideas:
    //[ ] Selection block - select any block you want (Diamond Block)
    //[ ] Rapid-fire Dispenser - 3 shots then pause (Dispenser)
    //[ ] Crumbling block - 2 uses then gone (Stone Brick, Cracked Stone Brick)
    //[ ] Disguise block - looks like a positive block for first use, applies negative affect, then stone
    //[ ] Lightning block - strikes lightning (Yellow wool)
    //[ ] Explosion block - creates explosion and launches you vertically (tnt with pressed pressure plate)
    //[/] Ghost block - players fall through (Diorite)
    //[/] Fireball Dispenser (Dispenser)
    //[ ] Effect Arrow Dispenser? (Dispenser)
    //[/] Kill block that can't be cleared (Red Sand)
    //[ ] Random direction velocity apply (cycles)
}
