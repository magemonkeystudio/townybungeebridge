package me.travja.townybridge.listeners.nation;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.NationCapitalChangeEvent;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BNationCapitalChangeEvent implements Listener {

    private static final String CACHE_STR = "changecapital";

    @EventHandler
    public void changeCapital(NationCapitalChangeEvent event) {
        Nation nation = event.getNation();
        Town capital = event.getNewCapital();
        if (CacheUtils.checkCache(CACHE_STR, nation.getUuid(), capital.getUuid())) {
            CacheUtils.clearCache(CACHE_STR, nation.getUuid());
            return;
        }

        CacheUtils.addCache(CACHE_STR, nation.getUuid(), capital.getUuid(), 60L);
        BungeeUtil.sendMessage(event.getEventName(), nation.getUuid().toString(), capital.getUuid().toString());
    }

    public static void received(UUID nID, UUID tID) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Nation nation = towny.getNation(nID);
            Town town = towny.getTown(tID);

            CacheUtils.addCache(CACHE_STR, nation.getUuid(), town.getUuid(), 60L);
            if (!nation.hasTown(town))
                nation.addTown(town);
            nation.setCapital(town);
        } catch (NotRegisteredException e) {
            Main.log.info("Attempted to add a town to a nation, but one is not registered.");
        } catch (AlreadyRegisteredException e) {
            //Already added
        } catch (TownyException e) {
            //It was cancelled.
        }
    }

}
