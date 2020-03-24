package me.travja.townybridge.listeners.nation;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.NationTagChangeEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BNationTagChangeEvent implements Listener {

    private static final String CACHE_STR = "nationchangetag";

    @EventHandler
    public void tagChange(NationTagChangeEvent event) {
        Nation nation = event.getNation();
        String tag = nation.getTag();

        if (CacheUtils.checkCache(CACHE_STR, nation.getUuid(), tag)) {
            CacheUtils.clearCache(CACHE_STR, nation.getUuid());
            return;
        }

        if (nation.getTag().equals(event.getNewTag()))
            return;

        CacheUtils.addCache(CACHE_STR, nation.getUuid(), event.getNewTag(), 60L);
        BungeeUtil.sendMessage(event.getEventName(), nation.getUuid().toString(), event.getNewTag());
    }

    public static void received(UUID id, String tag) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Nation nation = towny.getNation(id);
            nation.setTag(tag);
            CacheUtils.addCache(CACHE_STR, nation.getUuid(), tag, 60L);
        } catch (TownyException e) {
            Main.log.info("Nation is not registered!");
        }
    }

}
