package me.travja.townybridge.listeners.town;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.TownAddResidentRankEvent;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheData;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class BTownAddResidentRankEvent implements Listener {

    private static HashMap<String, CacheData> cached = new HashMap<>();

    {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();
        for (Town town : towny.getTowns()) {
            if (!town.hasMeta())
                continue;

            for (CustomDataField meta : town.getMetadata()) {
                if (!(meta instanceof StringDataField))
                    continue;

                StringDataField data = (StringDataField) meta;
                if (!data.getKey().startsWith("residentrank"))
                    continue;

                String[] dat = data.getValue().split("~");
                String name = dat[0];
                String rank = dat[1];

                cached.put(name, new CacheData("resrank", town.getUuid(), rank));
            }
        }
    }

    @EventHandler
    public void addRes(TownAddResidentRankEvent event) {
        Town town = event.getTown();
        Resident res = event.getResident();
        String rank = event.getRank();

        if (CacheUtils.checkCache("townaddrank", town.getUuid(), res.getName())) {
            CacheUtils.clearCache("townaddrank", town.getUuid());
            return;
        }

        BungeeUtil.sendMessage(event.getEventName(), town.getUuid().toString(), res.getName(), rank);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        if (!cached.containsKey(event.getPlayer().getName().toLowerCase()))
            return;

        Town town;
        try {
            town = TownyAPI.getInstance().getDataSource().getTown((UUID) cached.get(event.getPlayer().getName().toLowerCase()).getObj1());
            String rank = (String) cached.get(event.getPlayer().getName().toLowerCase()).getObj2();
            Resident res = TownyAPI.getInstance().getDataSource().getResident(event.getPlayer().getName());
            if (!town.hasResident(res))
                res.setTown(town);

            res.addTownRank(rank);
            Main.log.info("Set " + event.getPlayer().getName() + " rank to " + town.getName());

            if (town.hasMeta() && town.getMetadata().contains("residentrank")) //Clear out our metadata
                town.getMetadata().remove("residentrank");
            cached.remove(event.getPlayer().getName().toLowerCase());
        } catch (TownyException e) {
            e.printStackTrace();
        }
    }

    public static void received(UUID id, String resName, String rank) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Town town = towny.getTown(id);
            Player player = Bukkit.getPlayer(resName);
            try {
                Resident res = towny.getResident(player == null ? resName : player.getName());
                if (!town.hasResident(res)) //Make sure they're in the town
                    res.setTown(town);

                res.addTownRank(rank); //Add the rank
                Main.log.info("Added resident rank, " + rank);
            } catch (NotRegisteredException e) {
                resName = resName.toLowerCase();
                town.addMetaData(new StringDataField("residentrank" + resName, resName + "~" + rank));
                cached.put(resName, new CacheData("resrank", town.getUuid(), rank));
                Main.log.info("Queueing player to be added to " + town.getName());
            } catch (AlreadyRegisteredException ex) {
                //Do nothing :)
            }
        } catch (NotRegisteredException e) {
            Main.log.info("Attempting to add player to a town, but the town is not registered!");
        }
    }

}
