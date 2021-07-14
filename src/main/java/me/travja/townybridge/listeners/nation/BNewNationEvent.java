package me.travja.townybridge.listeners.nation;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.NewNationEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class BNewNationEvent implements Listener {

    private static HashMap<UUID, UUID> cached = new HashMap<>();

    {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();
        for (Nation nation : towny.getNations()) {
            if (nation.getCapital() == null || nation.getCapital().hasMeta())
                continue;

            for (CustomDataField meta : nation.getCapital().getMetadata()) {
                if (!(meta instanceof StringDataField))
                    continue;

                StringDataField data = (StringDataField) meta;
                if (!data.getKey().equals("king"))
                    continue;

                cached.put(UUID.fromString(data.getValue()), nation.getUuid());
            }
        }
    }

    public static void received(String name, UUID id, UUID mID, String tag, UUID capID, String server) {
        try {
            TownyDataSource towny = TownyAPI.getInstance().getDataSource();

            towny.newNation(name);
            Nation nation = towny.getNation(name);

            nation.setUuid(id);
            nation.setTag(tag);
            nation.setCapital(towny.getTown(capID));
            nation.addMetaData(new StringDataField("homeserver", server));
            Player player = Bukkit.getPlayer(mID);
            try {
                Resident mayor = towny.getResident(player == null ? "---" : player.getName());
                mayor.setTown(nation.getCapital());
                nation.setKing(mayor);
            } catch (NotRegisteredException e) {
                //Mayor not a registered resident
                nation.getCapital().addMetaData(new StringDataField("king", mID.toString()));
                cached.put(mID, nation.getUuid());
                Main.log.info("Queued king UUID: " + mID.toString());
            }

            Main.saveTowny();
            Main.log.info("BungeeBridge created new nation called '" + name + "'");
        } catch (TownyException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void create(NewNationEvent event) {
        Nation nation = event.getNation();
        String name = nation.getName();
        UUID id = nation.getUuid();
        Resident mayor = nation.getKing();
        String tag = nation.getTag();
        Town capital = nation.getCapital();

        BungeeUtil.sendMessage(event.getEventName(), name, id.toString(), Bukkit.getPlayer(mayor.getName()).getUniqueId().toString(), tag, capital.getUuid().toString(), Main.server);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        if (!cached.containsKey(event.getPlayer().getUniqueId()))
            return;

        Nation nation;
        try {
            nation = TownyAPI.getInstance().getDataSource().getNation(cached.get(event.getPlayer().getUniqueId()));
            Resident res = TownyAPI.getInstance().getDataSource().getResident(event.getPlayer().getName());
            res.setTown(nation.getCapital());
            nation.setKing(res);

            Main.saveTowny();
            Main.log.info("Set king to " + event.getPlayer().getName());
        } catch (TownyException e) {
            e.printStackTrace();
        }
    }

}
