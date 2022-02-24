package me.steep.steepapi.objects.items;

import me.steep.steepapi.api.BattalionAPI;
import me.steep.steepapi.handlers.DataHandler;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BattalionItem {

    private final ItemStack itemStack;
    private final Set<BattalionAPI.BattalionItemType> types;
    private final UUID uid;

    /**
     * Please don't use this without first checking if your ItemStack is a BattalionItem using BattalionAPI
     */
    public BattalionItem(ItemStack item) {
        itemStack = item;
        uid = UUID.fromString(DataHandler.getNBTString(item, "Battalion_UUID"));
        types = getItemTypes(item);
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public Set<BattalionAPI.BattalionItemType> getTypes() {
        return this.types;
    }

    private Set<BattalionAPI.BattalionItemType> getItemTypes(ItemStack item) {
        Set<BattalionAPI.BattalionItemType> itemTypes = new HashSet<>();
        for(String s : DataHandler.getNBTString(item, "Battalion_ItemTypes").split(",")) {
            itemTypes.add(BattalionAPI.BattalionItemType.valueOf(s));
        }
        return itemTypes;
    }

    public UUID getUUID() {
        return this.uid;
    }
}
