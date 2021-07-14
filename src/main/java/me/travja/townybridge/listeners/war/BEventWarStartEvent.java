package me.travja.townybridge.listeners.war;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.EventWarEndEvent;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BEventWarStartEvent implements Listener {

    public static void received() {
        Main.log.info("Starting war!");
        CacheUtils.addCache("startwar", null, 60L);
        TownyUniverse.getInstance().startWarEvent();
        TownyUniverse.getInstance().getWarEvent().getWarSpoils().setBalance(0, "Bungee War");
    }

    @EventHandler
    public void warEnd(EventWarEndEvent event) {
        if (CacheUtils.checkCache("startwar", null)) {
            CacheUtils.clearCache("startwar", null);
            return;
        }

        CacheUtils.addCache("startwar", null, 60L);
        BungeeUtil.sendMessage(event.getEventName());
    }

}
