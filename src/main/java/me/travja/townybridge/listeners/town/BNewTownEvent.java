package me.travja.townybridge.listeners.town;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.NewTownEvent;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class BNewTownEvent implements Listener {
    //TESTED This has been tested and works as intended.
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
                if (!data.getKey().equals("mayor"))
                    continue;

                cached.put(UUID.fromString(data.getValue()), town.getUuid());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void create(NewTownEvent event) {
        Town town = event.getTown();
        String name = town.getName();
        UUID id = town.getUuid();
        Resident mayor = town.getMayor();
        String tag = town.getTag();
        String world = town.getWorld().getName();

        if (CacheUtils.checkCache("newtown", town.getUuid())) {
            CacheUtils.clearCache("newtown", town.getUuid());
            return;
        }

        BungeeUtil.sendMessage(event.getEventName(), name, id.toString(), Bukkit.getPlayer(mayor.getName()).getUniqueId().toString(), tag, world);
        BungeeUtil.sendBlocks(town);
    }


    @EventHandler
    public void join(PlayerJoinEvent event) {
        if (!cached.containsKey(event.getPlayer().getUniqueId()))
            return;

        Town town;
        try {
            town = TownyAPI.getInstance().getDataSource().getTown(cached.get(event.getPlayer().getUniqueId()));
            Resident res = TownyAPI.getInstance().getDataSource().getResident(event.getPlayer().getName());
            town.addResident(res);
            town.setMayor(res);
            Main.log.info("Set mayor to " + event.getPlayer().getName());
            if (town.hasMeta() && town.getMetadata().contains("mayor")) //Clear out our metadata
                town.getMetadata().remove("mayor");
            cached.remove(event.getPlayer().getUniqueId());

            Main.saveTowny();
        } catch (TownyException e) {
            e.printStackTrace();
        }
    }

    public static void received(String name, UUID id, UUID mID, String tag, String world) {
        try {
            TownyDataSource towny = TownyAPI.getInstance().getDataSource();

            towny.newTown(name); //Create the new town
            Town town = towny.getTown(name);
            town.setUuid(id);
            town.setTag(tag);
            town.addMetaData(new StringDataField("homeserver", world));
            town.setWorld(towny.getWorld(Bukkit.getServer().getWorlds().get(0).getName()));
            towny.getTowns().add(town);
            CacheUtils.addCache("newtown", town.getUuid(), 60L);
            Player player = Bukkit.getPlayer(mID);
            try {
                Resident mayor = towny.getResident(player == null ? "---" : player.getName());
                town.addResident(mayor);
                town.setMayor(mayor);
            } catch (NotRegisteredException e) {
                //Mayor not a registered resident
                town.addMetaData(new StringDataField("mayor", mID.toString()));
                cached.put(mID, town.getUuid());
                Main.log.info("Queued mayor UUID: " + mID.toString());
            }

            Main.saveTowny();
            Main.log.info("BungeeBridge created new town called '" + name + "'");
        } catch (TownyException e) {
            e.printStackTrace();
        }
    }

}
