package me.steep.steepapi;

import me.DeeCaaD.CrackShotPlus.cs.ModifiedCSUtility;
import me.steep.steepapi.api.BattalionAPI;
import me.steep.steepapi.api.GeneralAPI;
import me.steep.steepapi.api.handlers.EShieldHandler;
import me.steep.steepapi.api.handlers.SaberHandler;
import me.steep.steepapi.listeners.PlayerArmorListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Don't create a new instance (new SteepAPI()) just use SteepAPI.getInst() if you need an instance of this for whatever reason
 */
public class SteepAPI extends JavaPlugin {

    private final ModifiedCSUtility cs = new ModifiedCSUtility();

    private static SteepAPI instance;
    private static GeneralAPI gapi;
    private static BattalionAPI bapi;
    private EShieldHandler eshieldhander;
    private SaberHandler saberhandler;

    @Override
    public void onEnable() {

        initialize();

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

    public EShieldHandler getEShieldHandler() {
        return this.eshieldhander;
    }

    public SaberHandler getSaberHandler() {
        return this.saberhandler;
    }

    /**
     * CrackShot's API
     * @return The CrackShot API
     */
    public ModifiedCSUtility getCSUtil() {
        return this.cs;
    }

    private void initialize() {

        this.assignVariables();
        this.registerListeners();

    }

    private void assignVariables() {

        gapi = new GeneralAPI();
        bapi = new BattalionAPI();
        eshieldhander = new EShieldHandler();
        saberhandler = new SaberHandler();

    }

    private void registerListeners() {

        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new PlayerArmorListener(), this);

    }
}
