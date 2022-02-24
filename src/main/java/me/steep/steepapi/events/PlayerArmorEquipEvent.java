package me.steep.steepapi.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerArmorEquipEvent extends Event implements Cancellable, Listener {

    private static final HandlerList handlerList = new HandlerList();
    private final Player player;
    private final ItemStack oldArmor;
    private final ItemStack newArmor;
    private boolean canceled;

    public PlayerArmorEquipEvent(@NotNull Player player, ItemStack oldArmor, ItemStack newArmor) {
        this.player = player;
        this.oldArmor = oldArmor != null ? oldArmor : new ItemStack(Material.AIR);
        this.newArmor = newArmor != null ? newArmor : new ItemStack(Material.AIR);
    }


    /**
     * @return The player executing this event
     */
    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return The ItemStack that was un-equipped in this event
     */
    @NotNull
    public ItemStack getOldArmor() { return this.oldArmor; }


    /**
     * @return The ItemStack that was equipped in this event
     */
    @NotNull
    public ItemStack getNewArmor() { return this.newArmor; }

    @Override
    public boolean isCancelled() {
        return this.canceled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.canceled = b;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    private static HandlerList getHandlerList() { return handlerList; }

}

