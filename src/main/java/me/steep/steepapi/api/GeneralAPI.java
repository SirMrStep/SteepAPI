package me.steep.steepapi.api;

import com.SirBlobman.combatlogx.api.ICombatLogX;
import com.SirBlobman.combatlogx.api.utility.ICombatManager;
import me.steep.steepapi.objects.Cooldown;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Never make a new GeneralAPI(), just get it via SteepAPI.getGeneralAPI()
 */
public class GeneralAPI {

    //private static final SteepAPI main = SteepAPI.getInst();
    private final Map<String, Map<UUID, Cooldown>> cooldowns = new HashMap<>();

    public Map<String, Map<UUID, Cooldown>> getCooldowns() {
        return this.cooldowns;
    }

    /**
     * @param player The player you want to check for
     * @param name   The name of the cooldown
     * @return Whether the specified Cooldown is active
     */
    public boolean isOnCooldown(Player player, String name) {

        return cooldowns.containsKey(name) && cooldowns.get(name).containsKey(player.getUniqueId());

    }

    /**
     * @param player The player on cooldown
     * @param name   The name of the cooldown
     * @return The requested Cooldown or null
     */
    @Nullable
    public Cooldown getCooldown(Player player, String name) {

        if (isOnCooldown(player, name)) {
            return cooldowns.get(name).get(player.getUniqueId());
        }

        return null;

    }

    /**
     * Creates a new Cooldown for the player if one under the same name doesn't already exist otherwise it will
     * set the remaining time of the existing Cooldown to the timeInSeconds parameter
     *
     * @param name          The name of the Cooldown, see this as a sort of "group name"
     * @param player        The player that this cooldown will apply to
     * @param timeInSeconds The time the cooldown lasts in seconds
     * @return The created or edited Cooldown
     */
    @NotNull
    public Cooldown setCooldown(Player player, String name, int timeInSeconds) {

        Cooldown c;
        if (this.isOnCooldown(player, name)) {
            c = this.getCooldown(player, name);
            c.setRemainingTime(timeInSeconds);
        } else {
            c = new Cooldown(name, player, timeInSeconds);
            if (cooldowns.containsKey(name)) {
                cooldowns.get(name).put(player.getUniqueId(), c);
            } else {
                Map<UUID, Cooldown> cds = new HashMap<>();
                cds.put(player.getUniqueId(), c);
                cooldowns.put(name, cds);
            }
        }

        return c;

    }

    /**
     * Removes the specified Cooldown from the specified Player if it exists.
     *
     * @param player The player that has a Cooldown
     * @param name   The name of the Cooldown
     */
    public void removeCooldown(Player player, String name) {

        if (this.isOnCooldown(player, name)) this.getCooldown(player, name).remove();

    }

    /**
     * @param p The player to send the actionbar to
     * @param s The text to put in the actionbar (supports color codes)
     */
    public void sendActionBar(Player p, String s) {

        CraftPlayer cp = (CraftPlayer) p;
        IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + this.color(s) + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, ChatMessageType.a((byte) 2), UUID.randomUUID());
        cp.getHandle().b.sendPacket(ppoc);

    }

    /**
     * This method simply colors text using color codes, example: "&aCool" is the same as ChatColor.GREEN + "Cool"
     *
     * @param text The text to color
     * @return The colored text
     */
    public String color(String text) {

        return ChatColor.translateAlternateColorCodes('&', text);

    }

    /**
     * @param item The ItemStack to check
     * @return Whether the specified ItemStack is armor
     */
    public boolean isArmor(ItemStack item) {

        String type = item.getType().toString();
        return type.endsWith("_BOOTS") || type.endsWith("_LEGGINGS") || type.endsWith("_CHESTPLATE") || type.endsWith("_HELMET");

    }

    /**
     * @param player The player to check
     * @return Whether the specified player is in combat
     */
    public boolean isInCombat(Player player) {
        ICombatLogX plugin = (ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");
        ICombatManager combatManager = plugin.getCombatManager();
        return combatManager.isInCombat(player);
    }

}
