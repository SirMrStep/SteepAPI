package me.steep.steepapi.listeners;

import me.steep.steepapi.SteepAPI;
import me.steep.steepapi.api.GeneralAPI;
import me.steep.steepapi.events.PlayerArmorEquipEvent;
import me.steep.steepapi.objects.ArmorItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerArmorListener implements Listener {

    private final GeneralAPI gapi = SteepAPI.getGeneralAPI();

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {

        // Event is canceled || executor is not a player || nothing changed in the inventory || Clicked inventory = null || ...
        // Clicked inventory type is not InventoryType.PLAYER
        if(e.isCancelled() || e.getWhoClicked().getType() != EntityType.PLAYER || e.getAction() == InventoryAction.NOTHING ||
                e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.PLAYER) return;

        if (e.isShiftClick()) {

            ItemStack clickedItem = e.getCurrentItem() != null ? e.getCurrentItem() : new ItemStack(Material.AIR);

            if(!gapi.isArmor(clickedItem)) return;

            ArmorItem ai = new ArmorItem(clickedItem);

            if(e.getSlotType() == InventoryType.SlotType.ARMOR) {

                if(e.getWhoClicked().getInventory().firstEmpty() == -1) return;

                if (launchPlayerArmorEquipEvent((Player) e.getWhoClicked(), clickedItem, null).isCancelled()) e.setCancelled(true);

            } else {

                ItemStack equippedItem = e.getInventory().getItem(ai.getRawSlot());
                equippedItem = equippedItem != null ? equippedItem : new ItemStack(Material.AIR);

                if(equippedItem.getType() != Material.AIR) return;

                if (launchPlayerArmorEquipEvent((Player) e.getWhoClicked(), null, clickedItem).isCancelled()) e.setCancelled(true);

            }

            return;

        }

        // SlotType is not SlotType.ARMOR
        if (e.getSlotType() != InventoryType.SlotType.ARMOR) return;

        // Cursor item = Cursor or Air if Cursor is null
        ItemStack cursorItem = e.getCursor() != null ? e.getCursor() : new ItemStack(Material.AIR);
        // Clicked item = Clicked item or Air if Clicked item is null
        ItemStack clickedItem = e.getCurrentItem() != null ? e.getCurrentItem() : new ItemStack(Material.AIR);

        // Event was triggered by swapping with an item from the HotBar
        if (e.getAction() == InventoryAction.HOTBAR_SWAP) {

            Player p = (Player) e.getWhoClicked();
            // Change the Cursor to the item swapped from the HotBar
            ItemStack hotbarItem = p.getInventory().getItem(e.getHotbarButton());
            cursorItem = hotbarItem != null ? hotbarItem : new ItemStack(Material.AIR);

        }

        switch (e.getRawSlot()) {

            case 5 -> doICEvents("_HELMET", e, e.getAction(), e.getWhoClicked(), cursorItem, clickedItem);
            case 6 -> doICEvents("_CHESTPLATE", e, e.getAction(), e.getWhoClicked(), cursorItem, clickedItem);
            case 7 -> doICEvents("_LEGGINGS", e, e.getAction(), e.getWhoClicked(), cursorItem, clickedItem);
            case 8 -> doICEvents("_BOOTS", e, e.getAction(), e.getWhoClicked(), cursorItem, clickedItem);

        }

        Bukkit.broadcastMessage("ClickedItem: " + clickedItem.getType());
        Bukkit.broadcastMessage("CursorItem: " + cursorItem.getType());
        Bukkit.broadcastMessage("HotBarButton: " + e.getHotbarButton());
        Bukkit.broadcastMessage("Slot: " + e.getSlot());
        Bukkit.broadcastMessage("RawSlot: " + e.getRawSlot());
        Bukkit.broadcastMessage("SlotType: " + e.getSlotType());
        Bukkit.broadcastMessage("Action: " + e.getAction());

    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {

        // Event is canceled || Executor was not a player || Old cursor/Dragged ItemStack was air || Old cursor/Dragged ItemStack was not armor
        if (e.isCancelled() || e.getWhoClicked().getType() != EntityType.PLAYER || e.getOldCursor().getType() == Material.AIR ||
                !gapi.isArmor(e.getOldCursor())) {
            return;
        }

        ArmorItem ai = new ArmorItem(e.getOldCursor());
        // Old cursor/Dragged ItemStack did not end up in an armor slot
        if (!e.getNewItems().containsKey(ai.getRawSlot()) || !e.getNewItems().get(ai.getRawSlot()).equals(e.getOldCursor())) {
            return;
        }

        if (launchPlayerArmorEquipEvent((Player) e.getWhoClicked(), null, e.getOldCursor()).isCancelled()) {

            e.setCancelled(true);

        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        //Bukkit.broadcastMessage(e.getAction().toString());
        //Bukkit.broadcastMessage(e.useItemInHand().toString());

        if (e.useItemInHand() == Event.Result.DENY || e.getAction() == Action.PHYSICAL) return;

        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (e.getItem() == null || !gapi.isArmor(e.getItem())) return;

        ArmorItem ai = new ArmorItem(e.getItem());
        ItemStack equippedItem = e.getPlayer().getInventory().getItem(ai.getSlot());

        // There is an armor piece currently equipped
        if (equippedItem != null && equippedItem.getType() != Material.AIR) return;

        boolean blocked = e.getClickedBlock() != null && blocksArmorEquip(e.getClickedBlock()) && !e.getPlayer().isSneaking();

        // Clicked block doesn't allow you to equip armor
        if (blocked) {

            Material type = e.getClickedBlock().getType();

            if (type == Material.BELL || type == Material.COMPOSTER) e.setCancelled(true);

            //e.setCancelled(true);
            return;

        }

        if (launchPlayerArmorEquipEvent(e.getPlayer(), null, e.getItem()).isCancelled()) {

            e.setCancelled(true);

        }

    }

    private void doICEvents(String endsWith, InventoryClickEvent e, InventoryAction action, HumanEntity clicker, ItemStack cursorItem, ItemStack clickedItem) {

        // Neither the Cursor nor the Clicked item are the correct armor piece for the clicked slot
        if (!cursorItem.getType().toString().endsWith(endsWith) && !clickedItem.getType().toString().endsWith(endsWith)) {
            return;
        }

        switch (action) {

            case PLACE_ALL -> {

                if (launchPlayerArmorEquipEvent((Player) clicker, null, cursorItem).isCancelled()) {

                    e.setCancelled(true);

                }

            }

            case PLACE_ONE -> {

                // Clicked item is not air (you have just added 1 to the clicked item)
                if (clickedItem.getType() != Material.AIR) {
                    return;
                }

                if (launchPlayerArmorEquipEvent((Player) clicker, null, cursorItem).isCancelled()) {

                    e.setCancelled(true);

                }

            }

            case PICKUP_ALL, DROP_ALL_SLOT -> {

                if (launchPlayerArmorEquipEvent((Player) clicker, clickedItem, null).isCancelled()) {

                    e.setCancelled(true);

                }

            }

            case PICKUP_HALF, DROP_ONE_SLOT -> {

                // Clicked item's stack size is not 1
                if (clickedItem.getAmount() != 1) {
                    return;
                }

                if (launchPlayerArmorEquipEvent((Player) clicker, clickedItem, null).isCancelled()) {

                    e.setCancelled(true);

                }

            }

            case SWAP_WITH_CURSOR, HOTBAR_SWAP -> {

                if (launchPlayerArmorEquipEvent((Player) clicker, clickedItem, cursorItem).isCancelled()) {

                    e.setCancelled(true);

                }

            }

        }

    }

    private PlayerArmorEquipEvent launchPlayerArmorEquipEvent(Player player, ItemStack oldArmor, ItemStack newArmor) {

        PlayerArmorEquipEvent event = new PlayerArmorEquipEvent(player, oldArmor, newArmor);
        Bukkit.getPluginManager().callEvent(event);
        return event;

    }

    private boolean blocksArmorEquip(Block block) {

        Material type = block.getType();

        switch (type) {

            case CHEST, TRAPPED_CHEST, ENDER_CHEST, ENCHANTING_TABLE, COMPARATOR, REPEATER, LEVER, HOPPER, NOTE_BLOCK, LECTERN, DAYLIGHT_DETECTOR, BLAST_FURNACE, CARTOGRAPHY_TABLE,
                    SMITHING_TABLE, COMPOSTER, SMOKER, BARREL, LOOM, GRINDSTONE, STONECUTTER, FURNACE, DISPENSER, DROPPER, CRAFTING_TABLE, BEACON, BREWING_STAND, BELL -> {
                return true;
            }

        }

        return Tag.FENCE_GATES.isTagged(type) || Tag.BUTTONS.isTagged(type) || Tag.SIGNS.isTagged(type)
                || Tag.ANVIL.isTagged(type) || Tag.REDSTONE_ORES.isTagged(type) || Tag.SHULKER_BOXES.isTagged(type)
                || Tag.WOODEN_TRAPDOORS.isTagged(type) || Tag.WOODEN_DOORS.isTagged(type) || Tag.FLOWER_POTS.isTagged(type)
                || Tag.BEDS.isTagged(type);

    }


}
