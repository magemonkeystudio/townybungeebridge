package me.travja.townybridge.listeners.player;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.DeletePlayerEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BDeletePlayerEvent implements Listener {

    @EventHandler
    public void delete(DeletePlayerEvent event) {
        String name = event.getPlayerName();

        if (CacheUtils.checkCache("removeresident", name)) {
            CacheUtils.clearCache("removeresident", name);
            return;
        }

        CacheUtils.addCache("removeresident", name, 60L);
        BungeeUtil.sendMessage(event.getEventName(), name);
    }

    public static void received(String name) {
        try {
            CacheUtils.addCache("removeresident", name, 60L);
            TownyAPI.getInstance().getDataSource().removeResident(TownyAPI.getInstance().getDataSource().getResident(name));
        } catch (NotRegisteredException e) {
            //Already Deleted
        }
    }

}
