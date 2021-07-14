package me.travja.townybridge.listeners.nation;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.NationInviteTownEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.invites.Invite;
import com.palmergames.bukkit.towny.invites.InviteHandler;
import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.inviteobjects.TownJoinNationInvite;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BNationInviteTownEvent implements Listener {

    private static final String CACHE_STR = "invitetown";

    @EventHandler
    public void inviteTown(NationInviteTownEvent event) {
        Invite invite = event.getInvite();
        String sender = invite.getDirectSender().getName();
        Nation nation = (Nation) invite.getSender();
        Town town = (Town) invite.getReceiver();
        if (CacheUtils.checkCache(CACHE_STR, nation.getUuid(), town.getUuid())) {
            CacheUtils.clearCache(CACHE_STR, nation.getUuid());
            return;
        }

        CacheUtils.addCache(CACHE_STR, nation.getUuid(), town.getUuid(), 60L);
        BungeeUtil.sendMessage(event.getEventName(), sender, nation.getUuid().toString(), town.getUuid().toString());
    }

    public static void received(String sname, UUID nID, UUID tID) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Nation nation = towny.getNation(nID);
            Town town = towny.getTown(tID);

            Invite invite = new TownJoinNationInvite(null, town, nation);

            CacheUtils.addCache(CACHE_STR, nation.getUuid(), town.getUuid(), 60L);
            nation.newSentInvite(invite);
            InviteHandler.addInvite(invite);
        } catch (NotRegisteredException e) {
            Main.log.info("Attempted to add a town to a nation, but one is not registered.");
        } catch (TooManyInvitesException e) {
            //Too many invites. Oops.
        }
    }

}
