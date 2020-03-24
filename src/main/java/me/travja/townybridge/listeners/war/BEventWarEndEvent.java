package me.travja.townybridge.listeners.war;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.EventWarEndEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BEventWarEndEvent implements Listener {

    @EventHandler
    public void warEnd(EventWarEndEvent event) {
        //TODO How to implements this?
        //TODO add other war events
        TownyUniverse.getInstance().setWarEvent(null);
        TownyUniverse.getInstance().getWarEvent();
        TownyUniverse.getInstance().endWarEvent();
        TownyUniverse.getInstance().startWarEvent();
        TownyUniverse.getInstance().getWarEvent().getWarZone();
        TownyUniverse.getInstance().addWarZone(null);
    }

    public static void received() {

    }

}
