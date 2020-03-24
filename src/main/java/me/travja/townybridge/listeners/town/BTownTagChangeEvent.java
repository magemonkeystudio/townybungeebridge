package me.travja.townybridge.listeners.town;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.TownTagChangeEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BTownTagChangeEvent implements Listener {

    private static final String CACHE_STR = "townchangetag";

    @EventHandler
    public void change(TownTagChangeEvent event) {
        Town town = event.getTown();
        String tag = town.getTag();

        if (CacheUtils.checkCache(CACHE_STR, town.getUuid(), tag)) {
            CacheUtils.clearCache(CACHE_STR, town.getUuid());
            return;
        }

        if (town.getTag().equals(event.getNewTag()))
            return;

        CacheUtils.addCache(CACHE_STR, town.getUuid(), event.getNewTag(), 60L);
        BungeeUtil.sendMessage(event.getEventName(), town.getUuid().toString(), event.getNewTag());
    }

    public static void received(UUID id, String tag) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Town town = towny.getTown(id);
            town.setTag(tag);
            CacheUtils.addCache(CACHE_STR, town.getUuid(), tag, 60L);
        } catch (TownyException e) {
            Main.log.info("Town is not registered!");
        }
    }

}
