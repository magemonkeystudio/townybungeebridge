package me.travja.townybridge.listeners.war;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.EventWarEndEvent;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BEventWarStartEvent implements Listener {

    @EventHandler
    public void warEnd(EventWarEndEvent event) {
        if (CacheUtils.checkCache("startwar", null)) {
            CacheUtils.clearCache("startwar", null);
            return;
        }

        CacheUtils.addCache("startwar", null, 60L);
        BungeeUtil.sendMessage(event.getEventName());
    }

    public static void received() {
        Main.log.info("Starting war!");
        CacheUtils.addCache("startwar", null, 60L);
        TownyUniverse.getInstance().startWarEvent();
        try {
            TownyUniverse.getInstance().getWarEvent().getWarSpoils().setBalance(0, "Bungee War");
        } catch (EconomyException e) {
            e.printStackTrace();
        }
    }

}
