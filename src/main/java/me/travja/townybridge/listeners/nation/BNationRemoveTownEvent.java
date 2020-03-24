package me.travja.townybridge.listeners.nation;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.NationRemoveTownEvent;
import com.palmergames.bukkit.towny.exceptions.EmptyNationException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BNationRemoveTownEvent implements Listener {

    @EventHandler
    public void addEnemy(NationRemoveTownEvent event) {
        Town town = event.getTown();
        Nation nation = event.getNation();
        if (CacheUtils.checkCache("remtown", nation.getUuid(), town.getUuid())) {
            CacheUtils.clearCache("remtown", nation.getUuid());
            return;
        }

        BungeeUtil.sendMessage(event.getEventName(), nation.getUuid().toString(), town.getUuid().toString());
    }

    public static void received(UUID nID, UUID tID) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Nation nation = towny.getNation(nID);
            Town town = towny.getTown(tID);

            CacheUtils.addCache("remtown", nation.getUuid(), town.getUuid(), 60L);
            nation.removeTown(town);
        } catch (NotRegisteredException e) {
            Main.log.info("Attempted to remove a town from a nation, but one is not registered.");
        } catch (EmptyNationException e) {
            Main.log.info("Nation is already empty.");
        }
    }

}
