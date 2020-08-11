package me.travja.townybridge.listeners.town;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.TownInvitePlayerEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.invites.Invite;
import com.palmergames.bukkit.towny.invites.InviteHandler;
import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.inviteobjects.PlayerJoinTownInvite;
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

public class BTownInvitePlayerEvent implements Listener {

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
                if (!data.getKey().startsWith("inviteplayer"))
                    continue;

                cached.put(data.getValue(), town.getUuid());
            }
        }
    }

    @EventHandler
    public void inviteTown(TownInvitePlayerEvent event) {
        Invite invite = event.getInvite();
        String sender = invite.getDirectSender();
        Town town = (Town) invite.getSender();
        Resident res = (Resident) invite.getReceiver();
        if (CacheUtils.checkCache("inviteplayer", town.getUuid(), res.getName())) {
            CacheUtils.clearCache("inviteplayer", town.getUuid());
            return;
        }


        BungeeUtil.sendMessage(event.getEventName(), sender, town.getUuid().toString(), res.getName());
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        if (!cached.containsKey(event.getPlayer().getName().toLowerCase()))
            return;

        Town town;
        try {
            town = TownyAPI.getInstance().getDataSource().getTown(cached.get(event.getPlayer().getUniqueId()));
            Resident res = TownyAPI.getInstance().getDataSource().getResident(event.getPlayer().getName());
            Invite invite = new PlayerJoinTownInvite(town.getName(), town, res);

            CacheUtils.addCache("inviteplayer", town.getUuid(), res.getName(), 60L);
            town.newSentInvite(invite);
            InviteHandler.addInvite(invite);

            if (town.hasMeta() && town.getMetadata().contains("inviteplayer" + event.getPlayer().getName().toLowerCase())) //Clear out our metadata
                town.getMetadata().remove("inviteplayer" + event.getPlayer().getName().toLowerCase());
            cached.remove(event.getPlayer().getName().toLowerCase());
        } catch (TownyException | TooManyInvitesException e) {
            e.printStackTrace();
        }
    }

    public static void received(String sname, UUID tID, String res) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        Player player = Bukkit.getPlayer(res);
        try {
            Town town = towny.getTown(tID);
            try {
                Resident resident = towny.getResident(player == null ? res : player.getName());

                Invite invite = new PlayerJoinTownInvite(sname, town, resident);

                CacheUtils.addCache("inviteplayer", town.getUuid(), resident.getName(), 60L);
                town.newSentInvite(invite);
                InviteHandler.addInvite(invite);
            } catch (NotRegisteredException e) {
                res = res.toLowerCase();
                town.addMetaData(new StringDataField("inviteplayer" + res, res));
                cached.put(res, town.getUuid());
                Main.log.info("Queueing player to be invited to " + town.getName());
            }
        } catch (TooManyInvitesException | NotRegisteredException e) {
            //Too many invites, town isn't registered. Oops.
        }
    }

}
