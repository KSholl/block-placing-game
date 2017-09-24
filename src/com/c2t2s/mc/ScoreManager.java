package com.c2t2s.mc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ScoreManager {

    private Map<String, Integer> scores;
    private String first, second;
    private int baseScore, firstBonus, secondBonus, winThreshold;
    private List<String> finishers;
    private List<Player> players, waitingOn;
    private MapInstance parent;
    private Objective objective;
    private Scoreboard scoreboard;
    private boolean scoreboardVisible;

    public ScoreManager(List<Player> players, List<Player> waitingOn,
            int baseScore, int firstBonus, int secondBonus, int winThreshold,
            MapInstance parent) {
        this.players = players;
        this.waitingOn = waitingOn;
        this.baseScore = baseScore;
        this.firstBonus = firstBonus;
        this.secondBonus = secondBonus;
        this.winThreshold = winThreshold;
        this.parent = parent;
        finishers = new ArrayList<>();
        scores = new HashMap<>();
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("score", "dummy");
        objective.setDisplayName(ChatColor.YELLOW + "Score:");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void add(Player player) {
        String name = player.getName();
        if (!scores.containsKey(name)) {
            scores.put(name, 0);
        }
        if (scoreboardVisible) {
            objective.getScore(name).setScore(scores.get(name));
            player.setScoreboard(scoreboard);
        }
    }

    public void finished(Player player) {
        if (waitingOn.contains(player)) {
            if (first == null) {
                first = player.getName();
            } else if (second == null) {
                second = player.getName();
            } else {
                finishers.add(player.getName());
            }
        }
    }

    public String roundEnd() {
        String message = "Round Results:\n";
        if (first == null) {
            parent.setPhase(MapInstance.Phase.BLOCKPENDING);
            message += "Nobody finished!\n" +
                    "Starting next round...";
            Bukkit.getScheduler().scheduleSyncDelayedTask(parent.getParent(),
                    new Runnable() {
                @Override
                public void run() {
                    parent.roundWithoutWinner();
                }
            }, 1L);
        } else if (second == null) {
            scores.put(first, scores.get(first) + baseScore
                    + firstBonus);
            message += "First Place: " + first + testForWinner();
            first = null;
        } else {
            scores.put(first, scores.get(first) + baseScore + firstBonus);
            scores.put(second, scores.get(second) + baseScore + secondBonus);
            String finished = ", ";
            if (finishers.size() > 0) {
                finished = "Completed the course: ";
            }
            for (String name: finishers) {
                scores.put(name, scores.get(name) + baseScore);
                finished += name + ", ";
            }
            message += "First Place: " + first + "\nSecond Place: " + second
                    + "\n" + finished.substring(0, finished.length() - 2)
                    + testForWinner();
            clearRoundVariables();
        }
        for (String name: scores.keySet()) {
            objective.getScore(name).setScore(scores.get(name));
        }
        return message;
    }

    //TODO: Make receiving end display the message
    public String deathMatchEnd(final Player player) {
        if (!waitingOn.contains(player)) {
            parent.getParent().getLogger().log(Level.WARNING, "Player "
                    + player.getName() + " was claimed to win deathmatch on "
                    + "world " + parent.getWorld().getName()
                    + " but was not part of the deathmatch");
            return null;
            //TODO: Make receiving end handle null value
        }
        String winner = player.getName();
        scores.put(winner, scores.get(winner) + firstBonus + baseScore);
        objective.getScore(winner).setScore(scores.get(winner));
        parent.setPhase(MapInstance.Phase.LOBBYPENDING);
        firework(player);
        return ChatColor.GOLD + player.getName() + ChatColor.RESET
                + " is the winner with " + ChatColor.GOLD + scores.get(winner)
                + ChatColor.RESET + " points!\nReturning to lobby...";
    }

    private String testForWinner() {
        List<Player> winners = new ArrayList<>();
        for (Player p: players) {
            if (scores.get(p.getName()) != null && scores.get(p.getName())
                    >= winThreshold) {
                if(winners.size() > 0) {
                    if (scores.get(p.getName()) > scores.get(winners.get(0)
                            .getName())) {
                        winners.clear();
                        winners.add(p);
                    } else if (scores.get(p.getName()).equals(scores
                            .get(winners.get(0).getName()))) {
                        winners.add(p);
                    }
                } else {
                    winners.add(p);
                }
            }
        }
        if (winners.size() == 0) {
            parent.setPhase(MapInstance.Phase.BLOCKPENDING);
            return "\nStarting next round...";
        } else if (winners.size() == 1) {
            parent.setPhase(MapInstance.Phase.LOBBYPENDING);
            //TODO: Make sure player is tp'd before fireworked
            firework(winners.get(0));
            return "\n" + ChatColor.GOLD + winners.get(0).getName()
                    + ChatColor.RESET + " is the winner with " + ChatColor.GOLD
                    + scores.get(winners.get(0).getName()) + ChatColor.RESET
                    + " points!\nReturning to lobby...";
        }
        String message = "";
        if (winners.size() == 2) {
            message = winners.get(0).getName() + " and " + winners.get(1)
                    .getName();
        } else {
            for (int i = 0; i < winners.size() - 1; i++) {
                message += winners.get(i).getName() + ", ";
            }
            message += " and " + winners.get(winners.size() - 1).getName();
        }
        message += " are tied for victory! Sudden death!!!";
        waitingOn.clear();
        waitingOn.addAll(winners);
        parent.setPhase(MapInstance.Phase.DEATHMATCHPENDING);
        return message;
    }

    private void clearRoundVariables() {
        first = null;
        second = null;
        finishers.clear();
    }

    public void clearScoreboard() {
        scores.clear();
        if (!(scoreboard.getObjective("score") == null)) {
            objective.unregister();
        }
    }

    public void setScoreboardVisible(boolean visible) {
        if (visible) {
            if (scoreboard.getObjective("score") == null) {
                objective = scoreboard.registerNewObjective("score", "dummy");
                objective.setDisplayName(ChatColor.YELLOW + "Score:");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                scores.clear();
                for (Player p: players) {
                    scores.put(p.getName(), 0);
                }
            }
            for (Player p: parent.getWorld().getPlayers()) {
                p.setScoreboard(scoreboard);
            }
        } else {
            Scoreboard empty = Bukkit.getScoreboardManager().getNewScoreboard();
            for (Player p: parent.getWorld().getPlayers()) {
                p.setScoreboard(empty);
            }
        }
        scoreboardVisible = visible;
    }

    private void firework(Player p) {
        Firework firework = (Firework) p.getWorld().spawnEntity(
                p.getLocation().add(0.0, 0.0, 1.0), EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().flicker(true)
                .withColor(Color.BLUE).withFade(Color.NAVY)
                .with(FireworkEffect.Type.BALL).trail(false).build();
        meta.addEffect(effect);
        meta.setPower(0);
        firework.setFireworkMeta(meta);
        firework = (Firework) p.getWorld().spawnEntity(
                p.getLocation().add(0.0, 0.0, -1.0), EntityType.FIREWORK);
        meta = firework.getFireworkMeta();
        effect = FireworkEffect.builder().flicker(true)
                .withColor(Color.GREEN).withFade(Color.OLIVE)
                .with(FireworkEffect.Type.BALL).trail(false).build();
        meta.addEffect(effect);
        meta.setPower(0);
        firework.setFireworkMeta(meta);
        firework = (Firework) p.getWorld().spawnEntity(
                p.getLocation().add(1.0, 0.0, 0.0), EntityType.FIREWORK);
        meta = firework.getFireworkMeta();
        effect = FireworkEffect.builder().flicker(true)
                .withColor(Color.RED).withFade(Color.MAROON)
                .with(FireworkEffect.Type.BALL).trail(false).build();
        meta.addEffect(effect);
        meta.setPower(0);
        firework.setFireworkMeta(meta);
        firework = (Firework) p.getWorld().spawnEntity(
                p.getLocation().add(-1.0, 0.0, 0.0), EntityType.FIREWORK);
        meta = firework.getFireworkMeta();
        effect = FireworkEffect.builder().flicker(true)
                .withColor(Color.YELLOW).withFade(Color.ORANGE)
                .with(FireworkEffect.Type.BALL).trail(false).build();
        meta.addEffect(effect);
        meta.setPower(0);
        firework.setFireworkMeta(meta);
    }
}
