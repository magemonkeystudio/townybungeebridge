package me.travja.townybridge.listeners.nation;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.NationRemoveEnemyEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BNationRemoveEnemyEvent implements Listener {

    private static final String CACHE_STR = "remenemy";

    @EventHandler
    public void addEnemy(NationRemoveEnemyEvent event) {
        Nation eNation = event.getEnemy();
        Nation nation = event.getNation();
        if (CacheUtils.checkCache(CACHE_STR, nation.getUuid(), eNation.getUuid())) {
            CacheUtils.clearCache(CACHE_STR, nation.getUuid());
            return;
        }

        CacheUtils.addCache(CACHE_STR, nation.getUuid(), eNation.getUuid(), 60L);
        BungeeUtil.sendMessage(event.getEventName(), nation.getUuid().toString(), eNation.getUuid().toString());
    }

    public static void received(UUID nID, UUID eID) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Nation nation = towny.getNation(nID);
            Nation eNation = towny.getNation(eID);

            CacheUtils.addCache(CACHE_STR, nation.getUuid(), eNation.getUuid(), 60L);
            nation.removeEnemy(eNation);
        } catch (NotRegisteredException e) {
            Main.log.info("Attempted to remove enemies, but one of the nations is not registered.");
        }
    }

}
