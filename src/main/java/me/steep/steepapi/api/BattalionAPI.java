package me.steep.steepapi.api;

import com.google.common.base.Strings;
import me.steep.steepapi.SteepAPI;
import me.steep.steepapi.api.handlers.EShieldHandler;
import me.steep.steepapi.api.handlers.SaberHandler;
import me.steep.steepapi.handlers.DataHandler;
import me.steep.steepapi.objects.items.BattalionItem;
import me.steep.steepapi.objects.items.EShield;
import me.steep.steepapi.objects.items.JetPack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Never assign a new BattalionAPI(), just get it via SteepAPI.getBattalionAPI()
 */
public class BattalionAPI {

    private final SteepAPI main = SteepAPI.getInst();
    private final GeneralAPI gapi = SteepAPI.getGeneralAPI();
    private final Set<UUID> eShieldedPlayers = new HashSet<>();

    /**
     * This class contains methods to work with the EnergyShield
     * @return The EnergyShield handler
     */
    public EShieldHandler getEShieldHandler() {
        return main.getEShieldHandler();
    }

    /**
     * This class contains methods to work with the EnergySaber
     * @return The EnergySaber handler
     */
    public SaberHandler getSaberHandler() {
        return main.getSaberHandler();
    }

    /**
     * I loop through this every 2 seconds (if at least 1 player is using an EShield) for the EShield health indicator actionbar
     *
     * @return A Set of UUID's containing the UUID's of all players that are currently using an EShield
     */
    public Set<UUID> getEShieldedPlayers() {
        return this.eShieldedPlayers;
    }

    public void addEShieldedPlayer(Player p) {
        if (p != null) {
            this.eShieldedPlayers.add(p.getUniqueId());
        }
    }

    public void removeEShieldedPlayer(Player p) {
        if (p != null) {
            this.eShieldedPlayers.remove(p.getUniqueId());
        }
    }

    public boolean isSaber(ItemStack item) {
        return item != null && getBattalionItemTypes(item).contains(BattalionItemType.ENERGYSABER.toString());
    }

    /**
     * @return Whether the specified ItemStack is a JetPack
     */
    public boolean isJetPack(ItemStack item) {
        return item != null && getBattalionItemTypes(item).contains(BattalionItemType.JETPACK.toString());
    }

    /**
     * @return Whether the specified player is wearing a JetPack
     */
    public boolean hasJetPack(Player p) {
        return p != null && isJetPack(p.getInventory().getChestplate());
    }

    /**
     * @throws NullPointerException If specified item or player or item is null
     */
    @NotNull
    public JetPack getJetPack(Player p, ItemStack item) throws NullPointerException {
        if (p == null) {
            throw new NullPointerException("getJetPack(Player, ItemStack) specified Player is null.");
        } else if (item == null) {
            throw new NullPointerException("getJetPack(Player, ItemStack) specified ItemStack is null.");
        }
        return new JetPack(p, item);
    }

    /**
     * @throws NullPointerException If specified player is null
     */
    @NotNull
    public JetPack getJetPack(Player p) throws NullPointerException {
        if (p == null) {
            throw new NullPointerException("getJetPack(Player) specified Player is null.");
        }
        return new JetPack(p, p.getInventory().getChestplate());
    }

    /**
     * @return Whether the specified item is an EnergyShield
     */
    public boolean isEShield(ItemStack item) {
        return item != null && getBattalionItemTypes(item).contains(BattalionItemType.ENERGYSHIELD.toString());
    }

    /**
     * @return Whether the specified player is currently regenerating their EShield
     */
    public boolean isRegenningEShield(Player p) {
        BukkitScheduler s = Bukkit.getScheduler();
        return hasEShield(p) && p.hasMetadata("Battalion_ESRegenTask") &&
                (s.isQueued(p.getMetadata("Battalion_ESRegenTask").get(0).asInt()) || s.isCurrentlyRunning(p.getMetadata("Battalion_ESRegenTask").get(0).asInt()));
    }

    /**
     * @return True if this player has an active EShield that is currently scheduled for regeneration but is awaiting its time-before-recharge
     */
    public boolean isScheduledForEShieldRegen(Player p) {
        return isRegenningEShield(p) && p.hasMetadata("Battalion_ESRegenDelay");
    }

    /**
     * @param p The player owning the EShield
     * @return Whether the specified player has an EShield active/equipped
     */
    public boolean hasEShield(Player p) { // DONE
        return p != null && isEShield(p.getInventory().getChestplate());
    }

    /**
     * @param player The player owning the EShield
     * @param item   The "EShield" item
     * @return The requested EShield
     * @throws NullPointerException if specified player is null
     */
    @NotNull
    public EShield getEShield(Player player, ItemStack item) throws NullPointerException {
        if (player == null) {
            throw new NullPointerException("getEShield(Player, ItemStack) specified Player is null.");
        } else if (item == null) {
            throw new NullPointerException("getEShield(Player, ItemStack) specified ItemStack is null.");
        }
        return new EShield(player, item);
    }

    /**
     * @param player The player owning the EShield
     * @return The requested EShield
     * @throws NullPointerException if specified player is null
     */
    @NotNull
    public EShield getEShield(Player player) throws NullPointerException {
        if (player == null) {
            throw new NullPointerException("getEShield(Player) specified Player is null.");
        }
        return new EShield(player, player.getInventory().getChestplate());
    }

    /**
     * @param item The item to check for a related gemid for
     * @return The first gemid on the item related to config section "EnergyShields" or "" if not present
     */
    @Nullable
    public String getRelatedEShieldGemId(ItemStack item) {

        Set<String> gemIds = getMMOItemGemIds(item);
        Set<String> esConfig = main.getConfig().getConfigurationSection("EnergyShields").getKeys(false);
        for (String id : gemIds) {
            if (esConfig.contains(id)) {
                return id;
            }
        }

        return "";
    }

    /**
     * @return sample text lmao remind me to update this if you're reading it
     */
    @NotNull
    public Set<String> getMMOItemGemIds(ItemStack item) {
        Set<String> ids = new HashSet<>();
        String[] nbt = DataHandler.getNBTString(item, "MMOITEMS_GEM_STONES") != null ?
                DataHandler.getNBTString(item, "MMOITEMS_GEM_STONES").split("\"Id\":\"") : "".split("");
        for (int index = 0; index < nbt.length; index++) {
            if ((index + 1) < nbt.length) {
                ids.add(nbt[index + 1].split("\"")[0]);
            }
        }
        return ids;
    }

    /**
     * @return Whether the specified ItemStack is a BattalionItem
     */
    public boolean isBattalionItem(ItemStack item) {
        return item != null && DataHandler.hasNBT(item, "Battalion_UUID");
    }

    /**
     * Please make sure to use isBattalionItem(ItemStack) before this, or you WILL get errors
     *
     * @return The requested BattalionItem
     */
    @NotNull
    public BattalionItem getBattalionItem(ItemStack item) {
        return new BattalionItem(item);
    }

    /**
     * Please make sure to use isBattalionItem(ItemStack) before this, or you WILL get errors
     *
     * @return A list of strings (these strings each represent a BattalionItemType.toString())
     */
    @NotNull
    public Set<String> getBattalionItemTypes(ItemStack item) {
        return new HashSet<>(List.of(DataHandler.getNBTString(item, "Battalion_ItemTypes").split(",")));
    }

    /**
     * @param item The ItemStack to register
     * @param type The type of BattalionItem the ItemStack will become
     * @throws NullPointerException if specified ItemStack is null
     */
    public void registerBattalionItem(ItemStack item, BattalionAPI.BattalionItemType type) throws NullPointerException {

        if (item == null) {
            throw new NullPointerException("registerBattalionItem(ItemStack, BattalionItemType) specified ItemStack is null.");
        }

        if (!DataHandler.hasNBT(item, "Battalion_UUID")) {
            DataHandler.setNBTString(item, "Battalion_UUID", UUID.randomUUID().toString());
        }

        switch (type) {

            case JETPACK -> {

                DataHandler.setNBTString(item, "Battalion_JPFlightState", JetPack.FlightState.NONE.toString());
                DataHandler.setNBTBoolean(item, "Battalion_JPUsing", false);

            }

            case ENERGYSHIELD -> {

                String id = getRelatedEShieldGemId(item);
                //DataHandler.setNBTBoolean(item, "Battalion_ESUsing", false);
                DataHandler.setNBTDouble(item, "Battalion_ESHealth", 0D);
                DataHandler.setNBTDouble(item, "Battalion_ESMaxHealth", main.getConfig().getDouble("EnergyShields." + id + ".shield-health"));
                //DataHandler.setNBTDouble(item, "Battalion_ESExtraHealth", 0D);

            }

        }

        String t = type.toString();
        String existingTypes = DataHandler.getNBTString(item, "Battalion_ItemTypes");

        if (!existingTypes.equals("")) {
            t = existingTypes + "," + t;
        }

        DataHandler.setNBTString(item, "Battalion_ItemTypes", t);

    }

    /**
     * @param min The amount left of the max value
     * @param max The max progress value
     * @param bars The amount of bars this progressbar should have
     * @param color The color of the progress
     * @return The requested progressbar as a String
     */
    /*public String getProgressBar(double min, double max, int bars, String color) {
        double percent = min / max;
        int progressBars = (int) (bars * percent);

        return Strings.repeat(main.colors(color + "|"), progressBars)
                + Strings.repeat(main.colors("&7|"), bars - progressBars);
    }*/

    /**
     * @param min   The amount left of the max value
     * @param max   The max progress value
     * @param bars  The amount of bars this progressbar should have
     * @param color The color of the progress
     * @return The requested progressbar as a String
     */
    public String getProgressBar(double min, double max, int bars, String color) {

        double percent = min / max;
        int progressBars = (int) (bars * percent);
        return Strings.repeat(gapi.color(color + "|"), progressBars) + Strings.repeat(gapi.color("&7|"), bars - progressBars);

    }

    private boolean running;

    /**
     * Starts the actionbar runnable (only if inactive) that displays both the actionbar for EnergyShields and EnergySabers to all relevant players
     * (stops if nobody has an EnergyShield or EnergySaber active)
     */
    public void displayProgressBar() {

        Bukkit.broadcastMessage("triggered display");
        if (!running) {
            Bukkit.broadcastMessage("starting runnable");
            running = true;
            new BukkitRunnable() {

                @Override
                public void run() {

                    //Bukkit.broadcastMessage("running");
                    if (!running) {

                        Bukkit.broadcastMessage("running false, stopping");
                        this.cancel();

                    }

                    if (eShieldedPlayers.isEmpty()) {

                        Bukkit.broadcastMessage("map is empty, stopping");
                        running = false;
                        this.cancel();

                    }

                    Set<UUID> players = new HashSet<>(eShieldedPlayers);
                    players.forEach(id -> {

                        try {

                            Player p = Bukkit.getPlayer(id);
                            if (!isRegenningEShield(p) || isScheduledForEShieldRegen(p)) {

                                EShield shield = getEShield(p);
                                double min = shield.getShieldHealth(false);
                                double max = shield.getShieldMaxHealth(false);
                                int bars = main.getConfig().getInt("EnergyShields." + shield.getGemId() + ".progressbar.bars");
                                String color = main.getConfig().getString("EnergyShields." + shield.getGemId() + ".progressbar.color");
                                //Bukkit.broadcastMessage("min: " + (int) min + ", max: " + (int) max + ", bars: " + bars + "color: " + color);
                                //Bukkit.broadcastMessage("showing");
                                gapi.sendActionBar(p, getProgressBar(min, max, bars, color) + " &f" + (int) min + color + " ⛨"); // make a getActionBar method in EShield

                            }

                        } catch (Exception ignored) {
                        }

                    });

                }

            }.runTaskTimer(main, 0, 40L);

        }

    }

    /**
     * Use this to set the display above player's heads that shows their "health"
     * @param p The player to set the scoreboard health for
     * @param health The new health to display
     */
    public void setScoreboardHealth(Player p, double health) {
        p.getScoreboard().getObjective("health").getScore(p.getName()).setScore((int) Math.ceil(health));
    }

    public enum ProgressBarType {
        ENERGYSHIELD, ENERGYSABER
    }

    public enum BattalionItemType {
        JETPACK, ENERGYSABER, ENERGYSHIELD
    }

}
