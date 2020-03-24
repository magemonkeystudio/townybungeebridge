package me.travja.townybridge.listeners.player;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.RenameResidentEvent;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BRenameResidentEvent implements Listener {

    @EventHandler
    public void rename(RenameResidentEvent event) {
        Resident res = event.getResident();
        String name = res.getName();

        if (CacheUtils.checkCache("renameresident", event.getOldName(), name)) {
            CacheUtils.clearCache("renameresident", event.getOldName());
            return;
        }

        if (name.equals(event.getOldName()))
            return;

        BungeeUtil.sendMessage(event.getEventName(), event.getOldName(), name);
    }

    public static void received(String old, String name) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            towny.renamePlayer(towny.getResident(old), name);
            CacheUtils.addCache("renameresident", old, name, 60L);
        } catch (NotRegisteredException | AlreadyRegisteredException e) {
            Main.log.info("Attempted to rename Resident, but it was not registered, or the name already exists");
        }
    }

}
