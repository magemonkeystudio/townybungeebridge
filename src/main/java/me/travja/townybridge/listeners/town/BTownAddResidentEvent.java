package me.travja.townybridge.listeners.town;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class BTownAddResidentEvent implements Listener {

    private static HashMap<String, UUID> cached = new HashMap<>();

    {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();
        for (Town town : towny.getTowns()) {
            if (!town.hasMeta())
                continue;

            for (CustomDataField meta : town.getMetadata()) {
                if (!(meta instanceof StringDataField))
                    continue;

                StringDataField data = (StringDataField) meta;
                if (!data.getKey().startsWith("resident"))
                    continue;

                cached.put(data.getValue(), town.getUuid());
            }
        }
    }

    @EventHandler
    public void addRes(TownAddResidentEvent event) {
        Town town = event.getTown();
        Resident res = event.getResident();
        if (town == null || town.getUuid() == null || res == null)
            return;

        if (CacheUtils.checkCache("townaddres", town.getUuid(), res.getName())) {
            CacheUtils.clearCache("townaddres", town.getUuid());
            return;
        }

        BungeeUtil.sendMessage(event.getEventName(), town.getUuid().toString(), res.getName());
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        if (!cached.containsKey(event.getPlayer().getUniqueId()) && !cached.containsKey(event.getPlayer().getName().toLowerCase()))
            return;

        Town town;
        try {
            if (cached.containsKey(event.getPlayer().getUniqueId()))
                town = TownyAPI.getInstance().getDataSource().getTown(cached.get(event.getPlayer().getUniqueId()));
            else
                town = TownyAPI.getInstance().getDataSource().getTown(cached.get(event.getPlayer().getName().toLowerCase()));

            Resident res = TownyAPI.getInstance().getDataSource().getResident(event.getPlayer().getName());
            town.addResident(res);
            Main.log.info("Added " + event.getPlayer().getName() + " to " + town.getName());

            if (town.hasMeta() && town.getMetadata().contains("resident" + event.getPlayer().getUniqueId().toString())) //Clear out our metadata
                town.getMetadata().remove("resident" + event.getPlayer().getUniqueId().toString());

            if (town.hasMeta() && town.getMetadata().contains("residentname" + event.getPlayer().getName().toLowerCase())) //Clear out our metadata
                town.getMetadata().remove("residentname" + event.getPlayer().getName().toLowerCase());

            cached.remove(event.getPlayer().getUniqueId());
            cached.remove(event.getPlayer().getName().toLowerCase());
        } catch (TownyException e) {
            e.printStackTrace();
        }
    }

    public static void received(UUID id, String resName) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Town town = towny.getTown(id);
            if (town == null)
                return; // Town doesn't exist.. so we really can't do anything.

            Player player = Bukkit.getPlayer(resName);
            try {
                Resident res = towny.getResident(player == null ? "---" : player.getName());
                town.addResident(res);
                Main.log.info("Added resident to " + town.getName());
            } catch (NotRegisteredException e) {
                if (player == null) {
                    resName = resName.toLowerCase();
                    town.addMetaData(new StringDataField("residentname" + resName, resName));
                    cached.put(resName.toLowerCase(), town.getUuid());
                } else {
                    town.addMetaData(new StringDataField("resident" + player.getUniqueId().toString(), player.getUniqueId().toString()));
                    cached.put(player.getUniqueId().toString(), town.getUuid());
                }
                Main.log.info("Queueing player to be added to " + town.getName());
            } catch (AlreadyRegisteredException ex) {
                //Do nothing
            }
        } catch (NotRegisteredException e) {
            Main.log.info("Attempting to add player to a town, but the town is not registered!");
        }
    }

}
