package me.travja.townybridge.listeners.town;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BRenameTownEvent implements Listener {

    private static final String CACHE_STR = "renametown";

    //TESTED
    @EventHandler
    public void rename(RenameTownEvent event) {
        Town town = event.getTown();
        String name = town.getName();

        if (CacheUtils.checkCache(CACHE_STR, town.getUuid(), name)) {
            CacheUtils.clearCache(CACHE_STR, town.getUuid());
            return;
        }

        if (town.getName().equals(event.getOldName()))
            return;

        CacheUtils.addCache(CACHE_STR, town.getUuid(), name, 60L);
        BungeeUtil.sendMessage(event.getEventName(), town.getUuid().toString(), name);
    }

    public static void received(UUID id, String name) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Town town = towny.getTown(id);
            towny.renameTown(town, name);
            CacheUtils.addCache(CACHE_STR, town.getUuid(), name, 60L);
        } catch (NotRegisteredException e) {
            Main.log.info("Attempted to rename town, but it was not registered.");
        } catch (AlreadyRegisteredException e) {
            Main.log.info("Attempted to set name of town to " + name + " but the name is already in use.");
        }
    }

}
