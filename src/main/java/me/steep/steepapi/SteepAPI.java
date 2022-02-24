package me.steep.steepapi;

import com.SirBlobman.combatlogx.api.ICombatLogX;
import com.SirBlobman.combatlogx.api.utility.ICombatManager;
import me.steep.steepapi.api.BattalionAPI;
import me.steep.steepapi.api.GeneralAPI;
import me.steep.steepapi.listeners.PlayerArmorListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 */
public class SteepAPI extends JavaPlugin {

    private static SteepAPI instance;
    private static GeneralAPI gapi;
    private static BattalionAPI bapi;

    @Override
    public void onEnable() {

        gapi = new GeneralAPI();
        bapi = new BattalionAPI();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerArmorListener(), this);

    }

    @Override
    public void onLoad() {
        instance = this;
    }

    public void send(String message) {
        Bukkit.getConsoleSender().sendMessage(gapi.color("&c&lImportant: &r" + message));
    }

    /**
     * @return The instance of this plugin
     */
    public static SteepAPI getInst() {
        return instance;
    }

    public static GeneralAPI getGeneralAPI() {
        return gapi;
    }

    public static BattalionAPI getBattalionAPI() {
        return bapi;
    }

}
