package me.steep.steepapi.api.handlers;

import com.SirBlobman.combatlogx.api.ICombatLogX;
import me.steep.steepapi.SteepAPI;
import me.steep.steepapi.api.BattalionAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

// make this class look 1% better, currently it looks like a pile of shit
// Also this class is REALLY OLD from before i updated the other classes so
// change it to use the new methods

public class EShieldHandler {

    // This map contains every player with a damaged shield
    private final Map<Player, Double> eShield = new HashMap<>();
    // This map tracks all regenTasks currently running
    private final Map<Player, BukkitRunnable> regenTasks = new HashMap<>();

    private final SteepAPI main = SteepAPI.getInst();
    private final BattalionAPI bapi = SteepAPI.getBattalionAPI();


    // Used to apply an identifier and to give the player the absorption hearts from the shield
    public void applyEShield(Player player, ItemStack item) {

        //Bukkit.broadcastMessage("Applying shield...");

        // This checks if player already has a shield active
        // and removes it to replace it with the new one
        //if(bapi.hasEShield(player)) bapi.removeEShield(player);

        if(player.hasMetadata("HasEShield")) {
            removeEShield(player);
            //Bukkit.broadcastMessage("Removed previous shield");
        }

        double eShieldHealth = main.getConfig().getInt("EnergyShields." + bapi.getRelatedEShieldGemId(item) + ".shield-health");

        player.setAbsorptionAmount((player.getAbsorptionAmount() + eShieldHealth));
        player.setMetadata("HasEShield", new FixedMetadataValue(main, true));
        //Bukkit.broadcastMessage("Shield applied.");
    }

    // Used to remove the players shield by removing him from the eShield map and resetting his absorption
    public void removeEShield(Player player) {
        // Checks if there is a shield active with the HasEShield Metadata
        // and removes the shield if there is
        if(player.hasMetadata("HasEShield")) {
            eShield.remove(player);
            // Absorption effect check to make sure we don't
            // mess with player's vanilla obtained absorption
            if(player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
                player.setAbsorptionAmount(((player.getPotionEffect(PotionEffectType.ABSORPTION).getAmplifier() * 4) + 4));
            } else {
                player.setAbsorptionAmount(0D);
            }
            player.removeMetadata("HasEShield", main);
        }
    }

    // Used to keep track of shield's health by putting player's absorption into a map after he is damaged.
    public void damageEShield(Player player, ItemStack chestplate) {
        double newAbsorption;
        // Absorption effect check to make sure we don't
        // mess with player's vanilla obtained absorption
        if(player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
            int toSubtract = ((player.getPotionEffect(PotionEffectType.ABSORPTION).getAmplifier() * 4) + 4);
            newAbsorption = (player.getAbsorptionAmount() - toSubtract);
        } else {
            newAbsorption = player.getAbsorptionAmount();
        }
        eShield.put(player, newAbsorption);
        runRegenTask(player, bapi.getRelatedEShieldGemId(chestplate));
    }

    public void runRegenTask(Player player, String eShieldID) {
        // This checks if there is currently a regenTask of the same type active
        // regenTask of any type will keep regenerating all player's shields for as long as the task is active
        if(!main.getConfig().getBoolean("EnergyShields." + eShieldID + ".recharge.out-of-combat")) {
            if(containsEShieldMap(player)) {
                if(!hasRunningRegenTask(player)) {
                    // Variables need to run the BukkitRunnable at the right timings
                    long delay = main.getConfig().getLong("EnergyShields." + eShieldID + ".recharge.time-before-recharge");
                    long rate = main.getConfig().getLong("EnergyShields." + eShieldID + ".recharge.recharge-rate");
                    double amount = main.getConfig().getDouble("EnergyShields." + eShieldID + ".recharge.recharge-amount");
                    double maxAmount = main.getConfig().getDouble("EnergyShields." + eShieldID + ".shield-health");

                    BukkitRunnable runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            if(!bapi.getRelatedEShieldGemId(player.getInventory().getChestplate()).equals(eShieldID) || !containsEShieldMap(player)) {
                                // Stopping the runnable
                                regenTasks.remove(player);
                                Bukkit.broadcastMessage(player.getName() + "'s runnable stopped");
                                this.cancel();
                            } else {
                                Bukkit.broadcastMessage(player.getName() + "'s runnable running");
                                // This is where the damage map and the player's absorption get updated
                                if((getEShieldMap(player) + amount) >= maxAmount) {
                                    if(player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
                                        player.setAbsorptionAmount((((player.getPotionEffect(PotionEffectType.ABSORPTION).getAmplifier() * 4) + 4) + maxAmount));
                                    } else {
                                        player.setAbsorptionAmount(maxAmount);
                                    }
                                    removeEShieldMap(player);
                                    regenTasks.remove(player);
                                    Bukkit.broadcastMessage(player.getName() + "'s runnable stopped");
                                    this.cancel();
                                } else {
                                    putEShieldMap(player, (getEShieldMap(player) + amount));
                                    player.setAbsorptionAmount((player.getAbsorptionAmount() + amount));
                                }
                            }
                        }
                    };
                    startRegenTask(player, runnable, delay, rate);
                }
            }
        } else {
            if(!isInCombat(player) && containsEShieldMap(player) && !hasRunningRegenTask(player)) {

                // Variables need to run the BukkitRunnable at the right timings
                long delay = main.getConfig().getLong("EnergyShields." + eShieldID + ".recharge.combat-time.time-before-recharge");
                long rate = main.getConfig().getLong("EnergyShields." + eShieldID + ".recharge.combat-time.recharge-rate");
                double amount = main.getConfig().getDouble("EnergyShields." + eShieldID + ".recharge.combat-time.recharge-amount");
                double maxAmount = main.getConfig().getDouble("EnergyShields." + eShieldID + ".shield-health");

                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(isInCombat(player) || !bapi.isEShield(player.getInventory().getChestplate()) || !bapi.getRelatedEShieldGemId(player.getInventory().getChestplate()).equals(eShieldID) || !containsEShieldMap(player)) {
                            // Stopping the runnable
                            regenTasks.remove(player);
                            Bukkit.broadcastMessage(player.getName() + "'s runnable stopped");
                            this.cancel();
                        } else {
                            Bukkit.broadcastMessage(player.getName() + "'s runnable running");
                            // This is where the damage map and the player's absorption get updated
                            if((getEShieldMap(player) + amount) >= maxAmount) {
                                if(player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
                                    player.setAbsorptionAmount((((player.getPotionEffect(PotionEffectType.ABSORPTION).getAmplifier() * 4) + 4) + maxAmount));
                                } else {
                                    player.setAbsorptionAmount(maxAmount);
                                }
                                removeEShieldMap(player);
                                regenTasks.remove(player);
                                Bukkit.broadcastMessage(player.getName() + "'s runnable stopped");
                                this.cancel();
                            } else {
                                putEShieldMap(player, (getEShieldMap(player) + amount));
                                player.setAbsorptionAmount((player.getAbsorptionAmount() + amount));
                            }
                        }
                    }
                };
                startRegenTask(player, runnable, delay, rate);
            }
        }
    }

    // Getters and setters for eShield map for outside this class
    public void putEShieldMap(Player key, double shieldHealth) {
        eShield.put(key, shieldHealth);
    }

    public void removeEShieldMap(Player key) {
        eShield.remove(key);
    }

    public boolean containsEShieldMap(Player key) {
        return eShield.containsKey(key);
    }

    public double getEShieldMap(Player key) {
        if(!eShield.containsKey(key)) {
            return 80085D;
        }
        return eShield.get(key);
    }

    public boolean hasRunningRegenTask(Player player) {
        return regenTasks.containsKey(player);
    }

    public void startRegenTask(Player player, BukkitRunnable runnable, long delay, long rate) {
        regenTasks.put(player, runnable);
        runnable.runTaskTimer(main, delay, rate);
    }

    public void stopRunningRegenTask(Player player) {
        if(regenTasks.containsKey(player)) {
            if(!regenTasks.get(player).isCancelled()) {
                regenTasks.get(player).cancel();
                Bukkit.broadcastMessage(player.getName() + "'s runnable stopped");
            }
            regenTasks.remove(player);
        }
    }

    private boolean isInCombat(Player player) {
        ICombatLogX plugin = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");
        if(plugin != null) {
            return plugin.getCombatManager().isInCombat(player);
        }
        return false;
    }

}
