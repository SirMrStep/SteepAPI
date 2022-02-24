package me.steep.steepapi.objects;

import me.steep.steepapi.SteepAPI;
import me.steep.steepapi.api.GeneralAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cooldown {

    private final SteepAPI main = SteepAPI.getInst();
    private final GeneralAPI gapi = SteepAPI.getGeneralAPI();

    private final String name;
    private final UUID player;
    private final int timeInSeconds;
    private long cooldownTime = 0L;
    private boolean started = false;

    public Cooldown(String name, Player player, int timeInSeconds) {
        this.name = name;
        this.player = player.getUniqueId();
        this.timeInSeconds = timeInSeconds;
    }

    BukkitRunnable r = new BukkitRunnable() {
        @Override
        public void run() {
            Bukkit.broadcastMessage("forcestopped cooldown");
            remove();
        }
    };

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public boolean hasStarted() {
        return this.started;
    }

    /**
     * Starts the cooldown
     */
    public void start() {

        this.cooldownTime = (System.currentTimeMillis() / 1000) + this.timeInSeconds;
        r.runTaskLater(main, (this.timeInSeconds * 20L));
        this.addPlayer();
        this.started = true;

    }

    /**
     * Removes the cooldown
     */
    public void remove() {

        try {
            int taskID = r.getTaskId();
            BukkitScheduler scheduler = Bukkit.getScheduler();
            if(scheduler.isQueued(taskID) || scheduler.isCurrentlyRunning(taskID)) {
                scheduler.cancelTask(taskID);
            }
        } catch (IllegalStateException ignored) { }

        //Bukkit.broadcastMessage("removed cooldown");
        this.removePlayer();
        this.started = false;

    }

    /**
     * Resets the cooldown
     */
    public void reset() {

        this.cooldownTime = (System.currentTimeMillis() / 1000) + this.timeInSeconds;

        try {
            BukkitScheduler s = Bukkit.getScheduler();
            if(s.isCurrentlyRunning(r.getTaskId()) || s.isQueued(r.getTaskId())) r.runTaskLater(main, (this.timeInSeconds * 20L));
        } catch (IllegalStateException ignored) { }

        if(!this.hasPlayer()) this.addPlayer();

        if(!this.started) this.started = true;

    }

    /**
     * Set's the remaining time of this Cooldown
     */
    public void setRemainingTime(int timeInSeconds) {
        this.cooldownTime = (System.currentTimeMillis() / 1000) + timeInSeconds;
    }

    /**
     * @return The remaining time on the Cooldown as a Long (precise), use this for calculations and/or coding stuff based on the time left
     */
    public long getRemainingLong() {
        long value = this.hasStarted() ? this.cooldownTime - (System.currentTimeMillis() / 1000) : 0L;
        if(value <= 0) {
            this.remove();
            return 0L;
        }
        return value;
    }

    /**
     * @return The remaining time on the Cooldown as an Int (rounded up), use this for displaying the cooldown
     */
    public int getRemainingInt() {
        int value = this.hasStarted() ? (int) Math.ceil((this.cooldownTime - (System.currentTimeMillis() / 1000D))) : 0;
        if(value <= 0) {
            this.remove();
            return 0;
        }
        return value;
    }

    /**
     * @return Whether the player is included in the cooldown map
     */
    private boolean hasPlayer() {
        Map<String, Map<UUID, Cooldown>> cds = gapi.getCooldowns();
        return cds.containsKey(this.name) && cds.get(this.name).containsKey(this.player);
    }

    /**
     * Adds the player to the cooldown map
     */
    private void addPlayer() {
        if(gapi.getCooldowns().containsKey(this.name)) {
            gapi.getCooldowns().get(this.name).put(this.player, this);
        } else {
            Map<UUID, Cooldown> players = new HashMap<>();
            players.put(this.player, this);
            gapi.getCooldowns().put(this.name, players);
        }
    }

    /**
     * Removes the player from the cooldown map
     */
    private void removePlayer() {
        if(gapi.getCooldowns().containsKey(this.name)) {
            if(gapi.getCooldowns().get(this.name).size() == 0) {
                gapi.getCooldowns().remove(this.name);
            } else {
                gapi.getCooldowns().get(this.name).remove(this.player);
            }
        }
    }


}

