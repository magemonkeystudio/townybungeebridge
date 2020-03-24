package me.travja.townybridge.listeners.town;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.exceptions.EmptyTownException;
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

public class BTownRemoveResidentEvent implements Listener {

    private static HashMap<UUID, UUID> cached = new HashMap<>();

    {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();
        for (Town town : towny.getTowns()) {
            if (!town.hasMeta())
                continue;

            for (CustomDataField meta : town.getMetadata()) {
                if (!(meta instanceof StringDataField))
                    continue;

                StringDataField data = (StringDataField) meta;
                if (!data.getKey().startsWith("remresident"))
                    continue;

                cached.put(UUID.fromString(data.getValue()), town.getUuid());
            }
        }
    }

    @EventHandler
    public void remRes(TownRemoveResidentEvent event) {
        Town town = event.getTown();
        Resident res = event.getResident();

        if (CacheUtils.checkCache("townremres", town.getUuid(), res.getName())) {
            CacheUtils.clearCache("townremres", town.getUuid());
            return;
        }

        BungeeUtil.sendMessage(event.getEventName(), town.getUuid().toString(), res.getName());
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        if (!cached.containsKey(event.getPlayer().getUniqueId()))
            return;

        Town town;
        try {
            town = TownyAPI.getInstance().getDataSource().getTown(cached.get(event.getPlayer().getUniqueId()));
            Resident res = TownyAPI.getInstance().getDataSource().getResident(event.getPlayer().getName());
            town.removeResident(res);
            Main.log.info("Removed " + event.getPlayer().getName() + " from " + town.getName());

            if (town.hasMeta() && town.getMetadata().contains("remresident" + event.getPlayer().getUniqueId().toString())) //Clear out our metadata
                town.getMetadata().remove("remresident" + event.getPlayer().getUniqueId().toString());
            cached.remove(event.getPlayer().getUniqueId());
        } catch (TownyException e) {
            e.printStackTrace();
        } catch (EmptyTownException e) {
            //Already gone!
        }
    }

    public static void received(UUID id, String resName) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Town town = towny.getTown(id);
            Player player = Bukkit.getPlayer(resName);
            try {
                Resident res = towny.getResident(player == null ? "---" : player.getName());
                town.removeResident(res);
                Main.log.info("Removed resident from " + town.getName());
            } catch (NotRegisteredException e) {
                town.addMetaData(new StringDataField("remresident" + player.getUniqueId().toString(), player.getUniqueId().toString()));
                cached.put(player.getUniqueId(), town.getUuid());
                Main.log.info("Queueing player to be removed from " + town.getName());
            } catch (EmptyTownException ex) {
                //Do nothing
            }
        } catch (NotRegisteredException e) {
            Main.log.info("Attempting to remove player from a town, but the town is not registered!");
        }
    }

}
