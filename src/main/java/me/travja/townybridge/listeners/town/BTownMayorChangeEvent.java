package me.travja.townybridge.listeners.town;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.TownMayorChangeEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BTownMayorChangeEvent implements Listener {

    @EventHandler
    public void mayorChange(TownMayorChangeEvent event) {
        Town town = event.getTown();
        Resident mayor = event.getNewMayor();

        if (CacheUtils.checkCache("newmayor", town.getUuid(), mayor.getName())) {
            CacheUtils.clearCache("newmayor", town.getUuid());
            return;
        }

        if (town == null)
            return;

        if (mayor == null) {
            Main.getInstance().getLogger().info("Attempting to update Bungee with new Mayor for town " + town.getName() + " but the mayor is null.");
            return;
        }

        Main.getInstance().getLogger().info("Event: " + event.getEventName());
        Main.getInstance().getLogger().info("Town: " + town);
        Main.getInstance().getLogger().info("UUID: " + town.getUuid());
        Main.getInstance().getLogger().info("Mayor: " + mayor);
        Main.getInstance().getLogger().info( "Name: " + mayor.getName());

        BungeeUtil.sendMessage(event.getEventName(), town.getUuid().toString(), mayor.getName());
    }


    public static void received(UUID id, String mName) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Town town = towny.getTown(id);
            Resident mayor = towny.getResident(mName);

            CacheUtils.addCache("newmayor", town.getUuid(), mayor.getName(), 60L);
            if (!town.hasResident(mayor))
                town.addResident(mayor);
            town.setMayor(mayor);
            Main.log.info("Set new mayor to " + mName);
        } catch (NotRegisteredException e) {
            Main.log.info("Attempted to assign a mayor, but the town and/or resident doesn't exist!");
        } catch (TownyException e) {
            e.printStackTrace();
        }

    }

}
