package me.steep.steepapi.objects.items;


import me.steep.steepapi.SteepAPI;
import me.steep.steepapi.api.BattalionAPI;
import me.steep.steepapi.handlers.DataHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

@SuppressWarnings("all")
public class ESaber {

    private final BattalionAPI bapi = SteepAPI.getBattalionAPI();

    private final Player owner;
    private final ItemStack itemStack;
    private final UUID uid;

    public ESaber(Player player, ItemStack item) {
        this.itemStack = item;
        this.owner = player;
        this.uid = UUID.fromString(DataHandler.getNBTString(item, "Battalion_UUID"));
    }

    private void setStamina(Player player, double stamina) {
        player.setMetadata("Battalion_SaberStamina", new FixedMetadataValue(SteepAPI.getInst(), stamina));
    }

    private void damageStamina(Player player, double damage) {
        double currentStamina = player.getMetadata("Battalion_SaberStamina").get(0).asDouble();
        double newStamina = (currentStamina - damage);
        if(newStamina < 0) {
            this.setStamina(player, 0D);
        } else {
            this.setStamina(player, newStamina);
        }
        // TODO START STAMINA REGEN
    }

    private void damageStamina(double damage) {
        double currentStamina = this.owner.getMetadata("Battalion_SaberStamina").get(0).asDouble();
        double newStamina = (currentStamina - damage);
        if(newStamina < 0) {
            if(currentStamina > 0) {
                this.setStamina(this.owner, 0D);
            }
        } else {
            this.setStamina(this.owner, newStamina);
        }
        // TODO START STAMINA REGEN
    }

    public static double getStamina(Player player) {
        return player.getMetadata("Battalion_SaberStamina").get(0).asDouble();
    }

    public double getStamina() { // TODO DONE
        return this.owner.getMetadata("Battalion_SaberStamina").get(0).asDouble();
    }

    /*public void sendStaminaActionBar(Player player) { // TODO MAKE IT 50 BARS
        double configValue = main.getConfig().getDouble("Saber.stamina");
        double percent = (getStamina(player) / configValue);
        double bars = configValue / 10;
        StringBuilder health = new StringBuilder();
        for(int index = 0; index < bars; index++) {
            if(index < Math.floor((bars * percent))) {
                health.append(main.colors("&c|"));
            } else {
                health.append(main.colors("&7|"));
            }
        }
        sendActionBar(player, Math.floor((percent * 100)) + "% " + health);
    }*/

}
