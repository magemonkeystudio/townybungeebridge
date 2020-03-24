package me.travja.townybridge.listeners.war;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.EventWarEndEvent;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BEventWarEndEvent implements Listener {

    @EventHandler
    public void warEnd(EventWarEndEvent event) {
        if (CacheUtils.checkCache("endwar", null)) {
            CacheUtils.clearCache("endwar", null);
            return;
        }

        CacheUtils.addCache("endwar", null, 60L);
        BungeeUtil.sendMessage(event.getEventName());
//        TownyUniverse.getInstance().setWarEvent(null);
//        TownyUniverse.getInstance().getWarEvent();
//        TownyUniverse.getInstance().endWarEvent();
//        TownyUniverse.getInstance().startWarEvent();
//        TownyUniverse.getInstance().getWarEvent().getWarZone();
//        TownyUniverse.getInstance().addWarZone(null);
    }

    public static void received() {
        CacheUtils.addCache("endwar", null, 60L);
        TownyUniverse.getInstance().endWarEvent();
        TownyUniverse.getInstance().getWarEvent().end();
    }

}
