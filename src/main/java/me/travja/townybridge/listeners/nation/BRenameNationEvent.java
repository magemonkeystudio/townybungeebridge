package me.travja.townybridge.listeners.nation;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.RenameNationEvent;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BRenameNationEvent implements Listener {

    @EventHandler
    public void rename(RenameNationEvent event) {
        Nation nation = event.getNation();
        String name = nation.getName();

        if (CacheUtils.checkCache("renamenation", nation.getUuid(), name)) {
            CacheUtils.clearCache("renamenation", nation.getUuid());
            return;
        }

        if (nation.getName().equals(event.getOldName()))
            return;

        BungeeUtil.sendMessage(event.getEventName(), nation.getUuid().toString(), name);
    }

    public static void received(UUID id, String name) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Nation nation = towny.getNation(id);
            towny.renameNation(nation, name);
            CacheUtils.addCache("renamenation", nation.getUuid(), name, 60L);
        } catch (NotRegisteredException e) {
            Main.log.info("Attempted to rename nation, but it was not registered.");
        } catch (AlreadyRegisteredException e) {
            Main.log.info("Attempted to rename nation to " + name + ", but the name is already in use.");
        }
    }

}
