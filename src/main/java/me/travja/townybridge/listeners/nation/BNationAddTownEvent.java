package me.travja.townybridge.listeners.nation;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.NationAddTownEvent;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BNationAddTownEvent implements Listener {

    private static final String CACHE_STR = "addtown";

    @EventHandler
    public void addEnemy(NationAddTownEvent event) {
        Town town = event.getTown();
        Nation nation = event.getNation();
        if (CacheUtils.checkCache(CACHE_STR, nation.getUuid(), town.getUuid())) {
            CacheUtils.clearCache(CACHE_STR, nation.getUuid());
            return;
        }

        CacheUtils.addCache(CACHE_STR, nation.getUuid(), town.getUuid(), 60L);
        BungeeUtil.sendMessage(event.getEventName(), nation.getUuid().toString(), town.getUuid().toString());
    }

    public static void received(UUID nID, UUID tID) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Nation nation = towny.getNation(nID);
            Town town = towny.getTown(tID);

            CacheUtils.addCache(CACHE_STR, nation.getUuid(), town.getUuid(), 60L);
            nation.addTown(town);
        } catch (NotRegisteredException e) {
            Main.log.info("Attempted to add a town to a nation, but one is not registered.");
        } catch (AlreadyRegisteredException e) {
            //Already added
        }
    }

}
