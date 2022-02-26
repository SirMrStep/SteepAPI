package me.steep.steepapi.objects.items;

import me.steep.steepapi.SteepAPI;
import me.steep.steepapi.api.BattalionAPI;
import me.steep.steepapi.api.GeneralAPI;
import me.steep.steepapi.handlers.DataHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.UUID;

public class EShield {

    private static final SteepAPI main = SteepAPI.getInst();
    private final BattalionAPI bapi = SteepAPI.getBattalionAPI();
    private final GeneralAPI gapi = SteepAPI.getGeneralAPI();

    private final Player owner;
    private ItemStack itemStack;
    private final UUID uid;
    private final String gemid;

    /**
     * @param player The wearer of this EShield
     * @param item   The "EShield" item
     */
    public EShield(Player player, ItemStack item) { // DONE
        this.owner = player;
        this.itemStack = item;
        this.uid = UUID.fromString(DataHandler.getNBTString(item, "Battalion_UUID"));
        this.gemid = bapi.getRelatedEShieldGemId(item);
    }

    /**
     * @return The player that owns this EShield
     */
    public Player getOwner() {
        return this.owner;
    }

    /**
     * @return The id of the gem belonging to this EShield
     */
    public String getGemId() { // DONE
        return this.gemid;
    }

    public String getUUID() {
        return this.uid.toString();
    }

    /**
     * @param updatedItemStack Whether the ItemStack belonging to this EShield should be updated
     *                         (Only use this if you think the ItemStack might have moved or been edited)
     * @return The health of this EShield
     */
    public double getShieldHealth(boolean updatedItemStack) { // DONE
        if (updatedItemStack) {
            this.itemStack = this.getItemStack();
        }
        return DataHandler.getNBTDouble(this.itemStack, "Battalion_ESHealth");
    }

    /**
     * @param updatedItemStack Whether the ItemStack belonging to this EShield should be updated
     *                         (Only use this if you think the ItemStack might have moved or been edited)
     * @return The max health of this EShield
     */
    public double getShieldMaxHealth(boolean updatedItemStack) { // DONE
        if (updatedItemStack) {
            this.itemStack = this.getItemStack();
        }
        return DataHandler.getNBTDouble(this.itemStack, "Battalion_ESMaxHealth");
    }

    /**
     * @param health           The new health this EShield should have
     * @param updatedItemStack Whether the ItemStack belonging to this EShield should be updated
     *                         (Only use this if you think the ItemStack might have moved or been edited)
     */
    public void setShieldHealth(double health, boolean updatedItemStack) { // DONE

        if (updatedItemStack) {
            this.itemStack = this.getItemStack();
        }

        DataHandler.setNBTDouble(this.itemStack, "Battalion_ESHealth", health);
        bapi.setScoreboardHealth(this.owner, this.owner.getHealth() + health);
    }

    /**
     * Note: this method updates the ItemStack
     *
     * @param damage The amount of damage this EShield should take as a double
     */
    public void damageShield(double damage) { // DONE

        double newHealth = this.getShieldHealth(false) - damage;

        setShieldHealth(newHealth, false);

        if (this.isRegenning()) {

            Bukkit.broadcastMessage("DAMAGE METHOD: is regenning, stopping");

            stopShieldRegen();

            this.owner.removeMetadata("Battalion_ESRegenTask", main);
            if (this.owner.hasMetadata("Battalion_ESRegenDelay")) {
                this.owner.removeMetadata("Battalion_ESRegenDelay", main);
            }
            //Bukkit.broadcastMessage("DAMAGE METHOD: removed metadata");

        }

        if (!main.getConfig().getBoolean("EnergyShields." + this.gemid + ".recharge.out-of-combat")) {

            startShieldRegen();

            /*Bukkit.broadcastMessage("DAMAGE METHOD: is combat shield");
            if(!main.isInCombat(this.owner)) {

                Bukkit.broadcastMessage("DAMAGE METHOD: owner is not in combat");
                //Bukkit.broadcastMessage("DAMAGE METHOD: player not in combat shield regen started for " + this.owner.getName());
                startShieldRegen();

            }*/
        } /*else {

            ///Bukkit.broadcastMessage("DAMAGE METHOD: not a combat shield");
            //Bukkit.broadcastMessage("DAMAGE METHOD: started shield regen for " + this.owner.getName());
            startShieldRegen();

        }*/

        showProgressBar();
    }

    private void setUsing(boolean using) {
        if (using) {
            this.owner.setMetadata("Battalion_ESUsing_" + this.uid.toString(), new FixedMetadataValue(main, true));
        } else {
            this.owner.removeMetadata("Battalion_ESUsing_" + this.uid.toString(), main);
        }
    }

    /**
     * @return Whether this EShield is currently being used
     */
    public boolean isBeingUsed() { // DONE
        return this.owner.hasMetadata("Battalion_ESUsing_" + this.uid.toString());
    }

    /**
     * @return Whether this EShield is currently regenerating
     */
    public boolean isRegenning() {
        BukkitScheduler s = Bukkit.getScheduler();
        return this.owner.hasMetadata("Battalion_ESRegenTask") &&
                (s.isQueued(this.owner.getMetadata("Battalion_ESRegenTask").get(0).asInt()) ||
                        s.isCurrentlyRunning(this.owner.getMetadata("Battalion_ESRegenTask").get(0).asInt()));
    }

    /**
     * @return True when this EShield is currently scheduled for regeneration but is awaiting its time-before-recharge
     */
    public boolean isScheduledForRegen() {
        return this.isRegenning() && this.owner.hasMetadata("Battalion_ESRegenDelay");
    }

    public int getRegenTaskID() {
        return this.isRegenning() ? this.owner.getMetadata("Battalion_ESRegenTask").get(0).asInt() : -1;
    }

    /**
     * Applies the EShield to its owner
     */
    public void applyEShield() {
        if (!this.isBeingUsed()) {
            this.setShieldHealth(0D, false);
            this.startShieldRegen();
            this.setUsing(true);
            bapi.setScoreboardHealth(this.owner, this.owner.getHealth() + this.getShieldHealth(false));
            this.showProgressBar();
            bapi.addEShieldedPlayer(this.owner);
            bapi.displayProgressBar();
        }
    }

    /**
     * Removes the EShield from its owner
     */
    public void removeEShield() { // DONE
        if (this.isBeingUsed()) {
            if (this.isRegenning()) {
                this.stopShieldRegen();
            }
            this.setUsing(false);
            gapi.sendActionBar(this.owner, "");
            bapi.removeEShieldedPlayer(this.owner);
        }
    }

    /**
     * @return The updated ItemStack belonging to this EShield
     */
    public ItemStack getItemStack() { // DONEs
        for (int index = 0; index < this.getOwner().getInventory().getSize(); index++) {
            ItemStack item = this.getOwner().getInventory().getItem(index);
            if (bapi.isBattalionItem(item)) {
                UUID id = UUID.fromString(DataHandler.getNBTString(item, "Battalion_UUID"));
                if (id.equals(this.uid)) {
                    return item;
                }
            }
        }
        return new ItemStack(Material.AIR);
    }

    /**
     * Starts regenerating this EShield
     * Note: this method updates the ItemStack
     */
    public void startShieldRegen() {

        //Bukkit.broadcastMessage("regen start triggered for " + this.owner.getName());
        Bukkit.broadcastMessage(getShieldHealth(false) + " < " + this.getShieldMaxHealth(false) + " && " + !this.owner.hasMetadata("Battalion_ESRegenTask") + ", " + this.owner.getName());
        if (!this.owner.hasMetadata("Battalion_ESRegenTask") && this.getShieldHealth(false) < this.getShieldMaxHealth(false)) {

            long delay = main.getConfig().getLong("EnergyShields." + this.gemid + ".recharge.time-before-recharge");
            long rate = main.getConfig().getLong("EnergyShields." + this.gemid + ".recharge.recharge-rate");
            double amount = main.getConfig().getDouble("EnergyShields." + this.gemid + ".recharge.recharge-amount");
            //Bukkit.broadcastMessage("regen task activated for " + this.owner.getName());

            BukkitRunnable r = new BukkitRunnable() {
                @Override
                public void run() {

                    //Bukkit.broadcastMessage("Runnable: running " + owner.getName());
                    if (main.getConfig().getBoolean("EnergyShields." + gemid + ".recharge.out-of-combat") && gapi.isInCombat(owner)) {

                        //Bukkit.broadcastMessage("u are in combat " + owner.getName());
                        removeRegenData();
                        this.cancel();
                        return;

                    }

                    //Bukkit.broadcastMessage(bapi.isEShield(owner.getInventory().getChestplate()) + ", " + getShieldHealth(true) + " < " + getShieldMaxHealth(false));
                    owner.removeMetadata("Battalion_ESRegenDelay", main);
                    if (bapi.isEShield(owner.getInventory().getChestplate()) && getShieldHealth(true) < getShieldMaxHealth(false)) {

                        if ((getShieldHealth(false) + amount) > getShieldMaxHealth(false)) {

                            setShieldHealth(getShieldMaxHealth(false), false);

                            if (rate <= 40) {

                                showProgressBar();

                            }

                            removeRegenData();
                            this.cancel();

                        } else {

                            double newHealth = getShieldHealth(false) + amount;
                            setShieldHealth(newHealth, false);

                            if (rate <= 40) {

                                showProgressBar();

                            }

                        }
                    } else {

                        if (rate <= 40) {

                            showProgressBar();

                        }

                        removeRegenData();
                        this.cancel();

                    }
                }
            };

            r.runTaskTimer(main, delay, rate);
            this.owner.setMetadata("Battalion_ESRegenTask", new FixedMetadataValue(main, r.getTaskId()));
            this.owner.setMetadata("Battalion_ESRegenDelay", new FixedMetadataValue(main, System.currentTimeMillis() / 1000));
            Bukkit.broadcastMessage("added metadata for " + this.owner.getName());

        }

    }

    private void showProgressBar() {

        double min = this.getShieldHealth(false);
        double max = this.getShieldMaxHealth(false);
        int bars = main.getConfig().getInt("EnergyShields." + gemid + ".progressbar.bars");
        String color = main.getConfig().getString("EnergyShields." + gemid + ".progressbar.color");
        gapi.sendActionBar(owner, bapi.getProgressBar(min, max, bars, color) + " &f" + (int) min + color + " ⛨");

    }

    /**
     * Stops regenerating this EShield
     */
    public void stopShieldRegen() { // DONE

        //Bukkit.broadcastMessage("regen stop triggered for " + this.owner.getName());
        if (this.isRegenning()) {

            //Bukkit.broadcastMessage("shield regen being stopped for " + this.owner.getName());
            int taskID = this.owner.getMetadata("Battalion_ESRegenTask").get(0).asInt();
            //Bukkit.broadcastMessage("taskid: " + taskID);

            if (Bukkit.getScheduler().isCurrentlyRunning(taskID) || Bukkit.getScheduler().isQueued(taskID)) {

                Bukkit.getScheduler().cancelTask(taskID);
                //Bukkit.broadcastMessage("cancellation confirmed for " + this.owner.getName());

            }

            this.owner.removeMetadata("Battalion_ESRegenTask", main);
            if (this.owner.hasMetadata("Battalion_ESRegenDelay")) {
                this.owner.removeMetadata("Battalion_ESRegenDelay", main);
            }

            //Bukkit.broadcastMessage("removed metadata for " + this.owner.getName());
            //bapi.displayProgressBar();

        }

    }

    private void removeRegenData() {

        this.owner.removeMetadata("Battalion_ESRegenTask", main);
        if (this.owner.hasMetadata("Battalion_ESRegenDelay")) {
            this.owner.removeMetadata("Battalion_ESRegenDelay", main);
        }

    }

}
