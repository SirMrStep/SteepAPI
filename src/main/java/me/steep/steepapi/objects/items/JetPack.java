package me.steep.steepapi.objects.items;

import me.steep.steepapi.SteepAPI;
import me.steep.steepapi.api.BattalionAPI;
import me.steep.steepapi.handlers.DataHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class JetPack {

    private final UUID owner;
    private final String gemid;
    private UUID uid;
    private final JPType type;
    private final float flightSpeed;
    private float boostSpeed = 0;
    private long boostDuration = 0;
    private long boostCooldown = 0;

    private final SteepAPI main = SteepAPI.getInst();
    private final BattalionAPI bapi = SteepAPI.getBattalionAPI();

    /**
     * @param player player who has the jetpack equipped
     * @param item the jetpack item
     * @throws IllegalStateException if jetpack gem failed to be found
     */
    public JetPack(Player player, ItemStack item) throws IllegalStateException {
        for (String gemId : bapi.getMMOItemGemIds(item)) {
            if (main.getConfig().getConfigurationSection("JetPacks").getKeys(false).contains(gemId)) {
                this.owner = player.getUniqueId();
                this.gemid = gemId;
                String varType = main.getConfig().getString("JetPacks." + this.gemid + ".hover-or-combat");
                this.type = varType.equalsIgnoreCase("hover") ? JPType.HOVER : JPType.COMBAT;
                this.flightSpeed = (float) main.getConfig().getDouble("JetPacks." + this.gemid + ".flight-speed");
                if (type == JPType.COMBAT) {
                    this.boostSpeed = (float) main.getConfig().getDouble("JetPacks." + this.gemid + ".boost-speed");
                    this.boostDuration = main.getConfig().getLong("JetPacks." + this.gemid + ".boost-duration");
                    this.boostCooldown = main.getConfig().getLong("JetPacks." + this.gemid + ".boost-cooldown");
                }
                try {
                    this.uid = UUID.fromString(DataHandler.getNBTString(item, "Battalion_UUID"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    main.send("Error trying to read/write NBT from " + player.getName() + "'s jetpack. This most likely means you have a faulty config.");
                }
                return;
            }
        }
        throw new IllegalStateException("Failed to find jetpack gem ID");
    }

    public Player getOwner() {
        return Bukkit.getPlayer(this.owner);
    }

    public String getId() {
        return this.gemid;
    }

    public JPType getType() {
        return this.type;
    }

    public float getFlightSpeed() {
        return this.flightSpeed;
    }

    public double getBoostSpeed() {
        return this.boostSpeed;
    }

    public double getBoostDuration() {
        return this.boostDuration;
    }

    public double getBoostCooldown() {
        return this.boostCooldown;
    }

    public long getBoostCooldownCounter() {
        ItemStack item = this.getItemStack();
        return DataHandler.hasNBT(item, "Battalion_JPCooldownCounter") ? DataHandler.getNBTLong(item, "Battalion_JPCooldownCounter") : 0L;
    }

    public long getBoostDurationCounter() {
        ItemStack item = this.getItemStack();
        return DataHandler.hasNBT(item, "Battalion_JPDurationCounter") ? DataHandler.getNBTLong(item, "Battalion_JPDurationCounter") : 0L;
    }

    public FlightState getFlightState() {
        ItemStack item = this.getItemStack();
        return item.getType() != Material.AIR ? FlightState.valueOf(DataHandler.getNBTString(item, "Battalion_JPFlightState")) : FlightState.NONE;
    }

    /**
     * This method should be used to start, stop and switch jetpack modes
     * @param state the new state the jetpack will be in
     */
    public void setFlightState(FlightState state) {
        DataHandler.setNBTString(this.getItemStack(), "Battalion_JPFlightState", state.toString());
        switch (state) {
            case COMBAT_FLYING, HOVER_FLYING, ON_COMBAT_BOOST_COOLDOWN -> {
                if (!this.isBeingUsed()) {
                    this.activate();
                }
            }
            case COMBAT_BOOSTING -> {
                if(!this.isBeingUsed()) {
                    this.activate();
                }
                DataHandler.setNBTLong(this.getItemStack(), "Battalion_JPCooldownCounter", ((System.currentTimeMillis() / 1000) + this.boostCooldown + this.boostDuration));
                DataHandler.setNBTLong(this.getItemStack(), "Battalion_JPDurationCounter", ((System.currentTimeMillis() / 1000) + this.boostDuration));
            }
            case NONE -> deActivate();
        }
    }

    public boolean isBeingUsed() {
        ItemStack item = this.getItemStack();
        return item.getType() != Material.AIR ? DataHandler.getNBTBoolean(item, "Battalion_JPUsing") : false;
    }

    public void activate() {
        DataHandler.setNBTBoolean(this.getItemStack(), "Battalion_JPUsing", true);
        Player p = this.getOwner();
        p.setAllowFlight(true);
        if (this.type == JPType.COMBAT) {
            p.setGliding(true);
            int heightLimit = main.getConfig().getInt("JetPacks.global_max_height_limit");
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (isBeingUsed() && bapi.hasJetPack(p) && !((Entity)p).isOnGround()) {
                            if(p.getLocation().getY() > heightLimit) {
                                setFlightState(FlightState.NONE);
                                Location loc = p.getLocation();
                                p.teleport(new Location(p.getWorld(), loc.getX(), 254, loc.getZ()));
                                this.cancel();
                                return;
                            }
                            switch (getFlightState()) {
                                case COMBAT_FLYING -> fly(p, flightSpeed);
                                case COMBAT_BOOSTING -> {
                                    if ((getBoostDurationCounter() - (System.currentTimeMillis() / 1000)) <= 0) {
                                        setFlightState(FlightState.ON_COMBAT_BOOST_COOLDOWN);
                                    }
                                    fly(p, boostSpeed);
                                }
                                case ON_COMBAT_BOOST_COOLDOWN -> {
                                    if ((getBoostCooldownCounter() - (System.currentTimeMillis() / 1000)) <= 0) {
                                        setFlightState(FlightState.COMBAT_FLYING);
                                    }
                                    fly(p, (float) (flightSpeed * 0.65));
                                }
                                case NONE -> deActivate();
                            }
                        } else if (!isBeingUsed()) {
                            this.cancel();
                        } else {
                            setFlightState(FlightState.NONE);
                        }
                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                        this.cancel();
                    }
                }
            }.runTaskTimer(main, 0, 1L);
        } else {
            p.setFlying(true);
            p.setFlySpeed(this.flightSpeed);
        }
    }

    /**
     * Never directly use this (it is only public because the plugin uses it in PlayerQuitEvent)
     */
    public void deActivate() {
        if(this.isBeingUsed()) {
            ItemStack item = this.getItemStack();
            DataHandler.setNBTBoolean(item, "Battalion_JPUsing", false);
            Player p = this.getOwner();
            p.setMetadata("Battalion_JPElytraCooldown", new FixedMetadataValue(main, ((System.currentTimeMillis() / 1000) + 6)));
            p.setAllowFlight(false);
            if (this.type == JPType.COMBAT) {
                p.setGliding(false);
            } else {
                p.setFlying(false);
                p.setFallDistance(0);
            }
        }
    }

    public boolean canBoostAgain() {
        return (this.getBoostCooldownCounter() - (System.currentTimeMillis() / 1000)) <= 0;
    }

    public boolean isBoosting() {
        return (this.getBoostDurationCounter() - (System.currentTimeMillis() / 1000)) > 0;
    }

    private void fly(Player player, Float flySpeed) {
        player.setVelocity(player.getEyeLocation().getDirection().multiply(flySpeed));
        player.setFallDistance(0.0F);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_BURN, 0.08F, 0.3F);
    }

    public ItemStack getItemStack() {
        for (int index = 0; index < this.getOwner().getInventory().getSize(); index++) {
            UUID indexItemUUID;
            try {
                indexItemUUID = UUID.fromString(DataHandler.getNBTString(this.getOwner().getInventory().getItem(index), "Battalion_UUID"));
            } catch (Exception ignored) {
                continue;
            }
            if (indexItemUUID.equals(this.uid)) {
                return this.getOwner().getInventory().getItem(index);
            }
        }
        return new ItemStack(Material.AIR);
    }

    public enum FlightState {
        NONE, COMBAT_FLYING, COMBAT_BOOSTING, ON_COMBAT_BOOST_COOLDOWN, HOVER_FLYING
    }

    public enum JPType {
        COMBAT, HOVER
    }

}
