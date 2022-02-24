package me.steep.steepapi.handlers;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

@SuppressWarnings("all")
public class DataHandler {

    /**
     * @param itemStack The ItemStack to edit
     * @param name The name to put on the ItemStack
     * @param lore The lore to put on the ItemStack
     */
    public static void setNameAndLore(ItemStack itemStack, String name, String... lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
    }

    /**
     * @param itemStack The ItemStack to edit
     * @param lore The lore to put on the ItemStack
     */
    public static void setLore(ItemStack itemStack, String... lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
    }

    /**
     * @param itemStack The ItemStack to edit
     * @param name The name to put on the ItemStack
     */
    public static void setName(ItemStack itemStack, String name) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setItemMeta(meta);
    }

    /**
     * @param itemStack The ItemStack to check
     * @param key The key to check for
     * @return Whether the specified ItemStack has the specified key in it's NBT tag
     */
    public static boolean hasNBT(ItemStack itemStack, String key) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        return nbttag.hasKey(key);
    }

    /**
     * @param itemStack The ItemStack to edit
     * @param key The key to remove from the specified ItemStack's NBT
     */
    public static void removeNBT(ItemStack itemStack, String key) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        nbttag.remove(key);
        item.setTag(nbttag);
        itemStack.setItemMeta(CraftItemStack.asBukkitCopy(item).getItemMeta());
    }

    /**
     * @param itemStack The ItemStack to edit
     * @param key The key to the specified String in the ItemStack's NBT
     * @param s The String to store in the specified ItemStack's NBT
     */
    public static void setNBTString(ItemStack itemStack, String key, String s) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        nbttag.setString(key, s);
        item.setTag(nbttag);
        itemStack.setItemMeta(CraftItemStack.asBukkitCopy(item).getItemMeta());
    }

    /**
     * @param itemStack The ItemStack to edit
     * @param key The key to the specified Boolean in the ItemStack's NBT
     * @param b The Boolean to store in the specified ItemStack's NBT
     */
    public static void setNBTBoolean(ItemStack itemStack, String key, Boolean b) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        nbttag.setBoolean(key, b);
        item.setTag(nbttag);
        itemStack.setItemMeta(CraftItemStack.asBukkitCopy(item).getItemMeta());
    }

    /**
     * @param itemStack The ItemStack to edit
     * @param key The key to the specified Integer in the ItemStack's NBT
     * @param b The Integer to store in the specified ItemStack's NBT
     */
    public static void setNBTInt(ItemStack itemStack, String key, Integer i) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        nbttag.setInt(key, i);
        item.setTag(nbttag);
        itemStack.setItemMeta(CraftItemStack.asBukkitCopy(item).getItemMeta());
    }

    /**
     * @param itemStack The ItemStack to edit
     * @param key The key to the specified Double in the ItemStack's NBT
     * @param b The Double to store in the specified ItemStack's NBT
     */
    public static void setNBTDouble(ItemStack itemStack, String key, Double d) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        nbttag.setDouble(key, d);
        item.setTag(nbttag);
        itemStack.setItemMeta(CraftItemStack.asBukkitCopy(item).getItemMeta());
    }

    /**
     * @param itemStack The ItemStack to edit
     * @param key The key to the specified Float in the ItemStack's NBT
     * @param b The Float to store in the specified ItemStack's NBT
     */
    public static void setNBTFloat(ItemStack itemStack, String key, Float f) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        nbttag.setFloat(key, f);
        item.setTag(nbttag);
        itemStack.setItemMeta(CraftItemStack.asBukkitCopy(item).getItemMeta());
    }

    /**
     * @param itemStack The ItemStack to edit
     * @param key The key to the specified Long in the ItemStack's NBT
     * @param b The Long to store in the specified ItemStack's NBT
     */
    public static void setNBTLong(ItemStack itemStack, String key, Long l) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        nbttag.setLong(key, l);
        item.setTag(nbttag);
        itemStack.setItemMeta(CraftItemStack.asBukkitCopy(item).getItemMeta());
    }

    /**
     * @param itemStack The ItemStack to edit
     * @param key The key to the specified Short in the ItemStack's NBT
     * @param b The Short to store in the specified ItemStack's NBT
     */
    public static void setNBTShort(ItemStack itemStack, String key, Short s) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        nbttag.setShort(key, s);
        item.setTag(nbttag);
        itemStack.setItemMeta(CraftItemStack.asBukkitCopy(item).getItemMeta());
    }

    /**
     * @param itemStack The ItemStack to edit
     * @param key The key to the specified Byte in the ItemStack's NBT
     * @param b The Byte to store in the specified ItemStack's NBT
     */
    public static void setNBTByte(ItemStack itemStack, String key, Byte b) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        nbttag.setByte(key, b);
        item.setTag(nbttag);
        itemStack.setItemMeta(CraftItemStack.asBukkitCopy(item).getItemMeta());
    }

    /**
     * @param itemStack The ItemStack to check
     * @param key The key to the specified String in the ItemStack's NBT
     * @return The requested String or "" if not present
     */
    public static String getNBTString(ItemStack itemStack, String key) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        return nbttag.getString(key);
    }

    /**
     * @param itemStack The ItemStack to check
     * @param key The key to the specified Boolean in the ItemStack's NBT
     * @return The requested Boolean or false if not present
     */
    public static Boolean getNBTBoolean(ItemStack itemStack, String key) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        return nbttag.getBoolean(key);
    }

    /**
     * @param itemStack The ItemStack to check
     * @param key The key to the specified Integer in the ItemStack's NBT
     * @return The requested Integer or 0 if not present
     */
    public static Integer getNBTInt(ItemStack itemStack, String key) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        return nbttag.getInt(key);
    }

    /**
     * @param itemStack The ItemStack to check
     * @param key The key to the specified Double in the ItemStack's NBT
     * @return The requested Double or 0.0D if not present
     */
    public static Double getNBTDouble(ItemStack itemStack, String key) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        return nbttag.getDouble(key);
    }

    /**
     * @param itemStack The ItemStack to check
     * @param key The key to the specified Float in the ItemStack's NBT
     * @return The requested Float or 0.0F if not present
     */
    public static Float getNBTFloat(ItemStack itemStack, String key) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        return nbttag.getFloat(key);
    }

    /**
     * @param itemStack The ItemStack to check
     * @param key The key to the specified Long in the ItemStack's NBT
     * @return The requested Long or 0L if not present
     */
    public static Long getNBTLong(ItemStack itemStack, String key) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        return nbttag.getLong(key);
    }

    /**
     * @param itemStack The ItemStack to check
     * @param key The key to the specified Short in the ItemStack's NBT
     * @return The requested Short or 0 if not present
     */
    public static Short getNBTShort(ItemStack itemStack, String key) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        return nbttag.getShort(key);
    }

    /**
     * @param itemStack The ItemStack to check
     * @param key The key to the specified Byte in the ItemStack's NBT
     * @return The requested Byte or 0 if not present
     */
    public static Byte getNBTByte(ItemStack itemStack, String key) {
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbttag = item.hasTag() ? item.getTag() : new NBTTagCompound();
        return nbttag.getByte(key);
    }

    /*private String getProgressBar(float percent, int size) {
        int progressBars = (int) Math.floor((size * percent));
        Bukkit.broadcastMessage(percent + "");

        return Strings.repeat(main.color("&6|"), progressBars)
                + Strings.repeat(main.color("&7|"), size - progressBars);
    }*/
}