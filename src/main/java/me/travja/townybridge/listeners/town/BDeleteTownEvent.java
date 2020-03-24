package me.travja.townybridge.listeners.town;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.PreDeleteTownEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BDeleteTownEvent implements Listener {
    //TESTED
    private HashMap<String, UUID> deleting = new HashMap<>();
    private static ArrayList<UUID> removingBungee = new ArrayList<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void preDelete(PreDeleteTownEvent event) {
        if (event.isCancelled())
            return;

        Town town = event.getTown();
        UUID id = town.getUuid();
        if (!removingBungee.contains(id))
            deleting.put(town.getName(), id);
        else
            removingBungee.remove(id);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void delete(DeleteTownEvent event) {
        String townName = event.getTownName();
        String evtName = event.getEventName();
        if (townName.startsWith("BUNGEE-"))
            return;

        if (deleting.containsKey(townName)) {
            //Transmit data across Bungee
            BungeeUtil.sendMessage(evtName, deleting.get(townName).toString());
            deleting.remove(townName);
        }
    }

    public static void received(UUID id) {
        try {
            TownyDataSource towny = TownyAPI.getInstance().getDataSource();
            Town town = towny.getTown(id);
            Main.log.info("Received delete request for town: " + town.getName() + ", uuid: " + id.toString());
            Main.log.info("Town exists? " + (town != null));
            if (town != null) {
                removingBungee.add(town.getUuid());
                towny.removeTown(town);
                Main.log.info("Deleted town.");
            }
        } catch (NotRegisteredException e) {
            //Already deleted
            Main.log.info("Town doesn't exist, so we're already done!");
        }
    }

}
