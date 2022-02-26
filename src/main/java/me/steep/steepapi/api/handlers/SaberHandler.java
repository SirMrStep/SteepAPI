package me.steep.steepapi.api.handlers;

import com.shampaggon.crackshot.events.WeaponShootEvent;
import me.DeeCaaD.CrackShotPlus.CSPapi;
import me.steep.steepapi.SteepAPI;
import me.steep.steepapi.api.GeneralAPI;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftSnowball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

// make this class look 1% better, currently it looks like a pile of shit
// Also this class is REALLY OLD from before i updated the other classes so
// change it to use the new methods

public class SaberHandler {

    private final Map<Player, Double> stamina = new HashMap<>();
    private boolean running = false;
    
    private final SteepAPI main = SteepAPI.getInst();
    private final GeneralAPI gapi = SteepAPI.getGeneralAPI();

    public void useSaber(Player damagedPlayer, Projectile proj, double appliedDamage) {

        double currentStamina = getStamina(damagedPlayer);

        if(currentStamina <= 0) {
            if(currentStamina < 0) {
                stamina.put(damagedPlayer, 0D);
            }
            return;
        } else if(currentStamina < appliedDamage) {
            damagedPlayer.damage((appliedDamage - currentStamina));
            stamina.put(damagedPlayer, 0D);
        } else {
            stamina.put(damagedPlayer, (getStamina(damagedPlayer) - appliedDamage));
        }

        if (main.getConfig().getBoolean("Saber.debug")) {
            damagedPlayer.sendMessage("You deflected: " + appliedDamage + " damage.");
            damagedPlayer.sendMessage("Your stamina is now: " + getStamina(damagedPlayer));
        }

        if(currentStamina > 0)  {
            String weaponTitle = main.getCSUtil().getWeaponTitle(proj);
            Vector target = damagedPlayer.getLocation().getDirection();
            Vector arrow = proj.getVelocity();
            target = target.normalize();
            arrow = arrow.normalize();
            if ((target.getX() * arrow.getX() + target.getZ() * arrow.getZ()) < -0.30) {
                deflectShot(damagedPlayer, proj, weaponTitle);
            }
        } else {
            damagedPlayer.sendMessage(ChatColor.RED + "You are out of stamina.");
        }

        startStaminaRegenTask();
    }

    //  shoots a new bullet to where the player is looking
    public void deflectShot(Player damagedPlayer, Projectile proj, String weaponTitle) {

        //e.setCancelled(true);
        Projectile csproj = damagedPlayer.launchProjectile(proj.getClass());
        main.getCSUtil().setProjectile(damagedPlayer, csproj, weaponTitle);
        boolean removeBulletDrop = CSPapi.getBoolean(weaponTitle + ".Shooting.Remove_Bullet_Drop") != null ? CSPapi.getBoolean(weaponTitle + ".Shooting.Remove_Bullet_Drop") : false;
        if (removeBulletDrop) {
            csproj.setGravity(false);
        }
        csproj.setMetadata("shitjectile", new FixedMetadataValue(main, true));
        Bukkit.getServer().getPluginManager().callEvent(new WeaponShootEvent(damagedPlayer, csproj, weaponTitle));
        //csproj.setMetadata("Deflected", new FixedMetadataValue(main, true));
        new BukkitRunnable() {
            @Override
            public void run() {
                PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(((CraftSnowball) csproj).getHandle().getId());
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (damagedPlayer.getLocation().distanceSquared(p.getLocation()) < 30000) {
                        ((CraftPlayer) p).getHandle().b.sendPacket(packet);
                    }
                }
            }
        }.runTaskLater(main, 0L);
    }

    public void startStaminaRegenTask() {
        if (!running) {
            running = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (stamina.isEmpty()) {
                        running = false;
                        this.cancel();
                    }
                    //Bukkit.broadcastMessage("Runnable running.");
                    double configValue = main.getConfig().getDouble("Saber.stamina");
                    for (Player player : stamina.keySet()) {
                        //Bukkit.broadcastMessage(player.getName() + "'s current stamina: " + getStamina(player));
                        double currentStamina = getStamina(player);
                        if (currentStamina < configValue) {
                            stamina.put(player, (currentStamina + (configValue * 0.05))); // make this configurable
                        }
                        if (stamina.get(player) >= configValue) {
                            stamina.remove(player);
                        }
                        //Bukkit.broadcastMessage(player.getName() + "'s new stamina: " + getStamina(player));
                    }
                }
            }.runTaskTimer(main, 0L, 20L);
        }
    }

    public double getStamina(Player player) {
        return stamina.getOrDefault(player, main.getConfig().getDouble("Saber.stamina"));
    }

    public void sendStaminaActionBar(Player player) {
        double configValue = main.getConfig().getDouble("Saber.stamina");
        double percent = (getStamina(player) / configValue);
        double bars = configValue / 10;
        StringBuilder health = new StringBuilder();
        for(int index = 0; index < bars; index++) {
            if(index < Math.floor((bars * percent))) {
                health.append(gapi.color("&c|"));
            } else {
                health.append(gapi.color("&7|"));
            }
        }
        gapi.sendActionBar(player, Math.floor((percent * 100)) + "% " + health);
    }

    public void setStamina(Player player, double amount) {
        stamina.put(player, amount);
    }

    public boolean containsStamina(Player player) {
        return stamina.containsKey(player);
    }

    public void removeStamina(Player player) {
        stamina.remove(player);
    }
}
