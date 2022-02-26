package me.steep.steepapi.handlers;

import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("all")
public class DamageCalc {

    /**
     * Calculates damage that should be done to a player after reductions such as armor and enchantments
     * This method is not 100% accurate, more like 98%, I don't suggest you use it to track health.
     * A use case for this would be removing points or something else from a player based on the amount
     * of damage they receive. 98% accuracy should be enough for such a case.
     *
     * @param damage The RAW damage done
     * @param player The player that is getting damaged
     * @return The damage that should be done to the player after damage reductions
     */
    public static double calcDMG(double damage, Player player) {

        double armor = player.getAttribute(Attribute.GENERIC_ARMOR).getValue();
        double toughness = player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
        int epf = getEPF(player);

        // Calculated damage after armor toughness calculations
        double armorAndToughnessCalc = damage*(1-(Math.min(20, Math.max(armor/5, armor-((4*damage)/(toughness+8))))/25));

        for(PotionEffect effect : player.getActivePotionEffects()) {

            if(effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {

                // Calculated damage after resistance calculations
                armorAndToughnessCalc = armorAndToughnessCalc*(1-(effect.getAmplifier()*0.2));
                break;

            }

        }

        // Calculated damage after protective enchantments calculations
        return armorAndToughnessCalc*(1-(Math.min(20.0, epf)/25));
    }

    private static int getEPF(Player player) {
        ItemStack helm = player.getInventory().getHelmet();
        ItemStack chest = player.getInventory().getChestplate();
        ItemStack legs = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        return (helm != null ? helm.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) : 0) +
                (chest != null ? chest.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) : 0) +
                (legs != null ? legs.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) : 0) +
                (boots != null ? boots.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) : 0);
    }

}
