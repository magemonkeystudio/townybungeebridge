package me.travja.townybridge.listeners.nation;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BDeleteNationEvent implements Listener {

    private static final String CACHE_STR = "delete";

    @EventHandler
    public void delete(DeleteNationEvent event) {
        String name = event.getNationName();
        if (CacheUtils.checkCache(CACHE_STR, name)) {
            CacheUtils.clearCache(CACHE_STR, name);
            return;
        }

        CacheUtils.addCache(CACHE_STR, name, 60L);
        BungeeUtil.sendMessage(event.getEventName(), name);
    }

    public static void received(String name) {
        try {
            TownyDataSource towny = TownyAPI.getInstance().getDataSource();
            Nation nation = towny.getNation(name);
            Main.log.info("Received delete request for nation: " + nation.getName());
            if (nation != null) {
                CacheUtils.addCache(CACHE_STR, name, 60L);
                towny.removeNation(nation);
                Main.log.info("Deleted nation.");
            }
        } catch (NotRegisteredException e) {
            //Already deleted
            Main.log.info("Nation doesn't exist, so we're already done!");
        }
    }

}
