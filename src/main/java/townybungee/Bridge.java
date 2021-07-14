package townybungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Bridge extends Plugin {

    private static Configuration config;
    private static Bridge instance;
    public static final String CHANNEL = "travja:townybridge";

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();

        if (!config.contains("sync"))
            config.set("sync", Arrays.asList("server1", "server2"));
        saveConfig();

        register();

        getLogger().info("TownyBungeeBridge has been enabled!");
    }

    @Override
    public void onDisable() {
        BungeeListener.save();
    }

    private void register() {
        PluginManager pm = getProxy().getPluginManager();
        pm.registerListener(this, new BungeeListener());
        getProxy().registerChannel(CHANNEL);
    }

    public static Bridge getInstance() {
        return instance;
    }

    public static Configuration getConfig() {
        if (config == null)
            loadConfig();

        return config;
    }

    private static void loadConfig() {
        try {
            File file = new File(instance.getDataFolder(), "config.yml");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(instance.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
