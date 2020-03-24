package me.travja.townybridge;

import com.google.common.collect.ImmutableSet;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.KeyAlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import me.travja.townybridge.listeners.BungeeListener;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.ClassPath;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    private FileConfiguration config;

    private static Main instance;
    public static String server;

    public static Logger log;

    public void onEnable() {
        instance = this;
        log = this.getLogger();
        config = this.getConfig();
        if (!new File(getDataFolder(), "config.yml").exists()) {
            this.saveDefaultConfig();
            config.options().copyDefaults(true);
        }

        server = config.getString("server");

        registerEvents();
    }

    public void onDisable() {

    }

    public void registerEvents() {
        try {
            TownyAPI.getInstance().registerCustomDataField(new StringDataField("homeserver", ""));
        } catch (KeyAlreadyRegisteredException e) {
            log.info("Couldn't register homeserver field.");
        }

        PluginManager pm = this.getServer().getPluginManager();

        String packageName = "me.travja.townybridge.listeners";
        ImmutableSet<ClassPath.ClassInfo> root;
        try {
            root = ClassPath.from(getClass().getClassLoader()).getTopLevelClassesRecursive(packageName);
            for (ClassPath.ClassInfo info : root) {
                Class clazz = info.load();
                if (Listener.class.isAssignableFrom(clazz)) {
                    pm.registerEvents((Listener) clazz.newInstance(), this);
                    Main.log.info("Registered: " + info.getSimpleName());
                }
            }
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            e.printStackTrace();
        }


/*        //Town Events
        pm.registerEvents(new BDeleteTownEvent(), this);
        pm.registerEvents(new BNewTownEvent(), this);
        pm.registerEvents(new BRenameTownEvent(), this);
        pm.registerEvents(new BTownAddResidentEvent(), this);
        pm.registerEvents(new BTownAddResidentRankEvent(), this);

        //Nation Events
        pm.registerEvents(new BDeleteNationEvent(), this);
        pm.registerEvents(new BNationAddEnemyEvent(), this);
        pm.registerEvents(new BNationAddTownEvent(), this);
        pm.registerEvents(new BNationInviteTownEvent(), this);
        pm.registerEvents(new BNationRemoveEnemyEvent(), this);
        pm.registerEvents(new BNationRemoveTownEvent(), this);
        pm.registerEvents(new BNationRequestAllyNationEvent(), this);
        pm.registerEvents(new BNationTagChangeEvent(), this);
        pm.registerEvents(new BNationTransactionEvent(), this);
        pm.registerEvents(new BNewNationEvent(), this);
        pm.registerEvents(new BRenameNationEvent(), this);

        //Player Events
        pm.registerEvents(new BDeletePlayerEvent(), this);
        pm.registerEvents(new BPlotChangeOwnerEvent(), this);
        pm.registerEvents(new BRenameResidentEvent(), this);

        //War Events
        pm.registerEvents(new BEventWarEndEvent(), this);*/

        //Register our stuff for Bungee
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, BungeeUtil.CHANNEL);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, BungeeUtil.CHANNEL, new BungeeListener());
    }

    public static Main getInstance() {
        return instance;
    }

    public static void saveTowny() {
        TownyAPI.getInstance().getDataSource().saveAll();
    }
}
