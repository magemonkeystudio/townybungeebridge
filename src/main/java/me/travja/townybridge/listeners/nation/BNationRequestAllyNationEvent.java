package me.travja.townybridge.listeners.nation;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.event.NationRequestAllyNationEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.invites.Invite;
import com.palmergames.bukkit.towny.invites.InviteHandler;
import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.inviteobjects.NationAllyNationInvite;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import me.travja.townybridge.util.CacheUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BNationRequestAllyNationEvent implements Listener {

    @EventHandler
    public void requestAlly(NationRequestAllyNationEvent event) {
        Invite invite = event.getInvite();
        Nation nation = (Nation) invite.getSender();
        Nation ally = (Nation) invite.getReceiver();

        if (CacheUtils.checkCache("requestally", nation.getUuid(), ally.getUuid())) {
            CacheUtils.clearCache("requestally", nation.getUuid());
        }

        BungeeUtil.sendMessage(event.getEventName(), invite.getDirectSender(), nation.getUuid().toString(), ally.getUuid().toString());
    }

    public static void received(String sname, UUID nID, UUID aID) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();

        try {
            Nation nation = towny.getNation(nID);
            Nation ally = towny.getNation(aID);

            Invite invite = new NationAllyNationInvite(sname, nation, ally);

            CacheUtils.addCache("requestally", nation.getUuid(), ally.getUuid(), 60L);
            nation.newSentInvite(invite);
            InviteHandler.addInvite(invite);
        } catch (NotRegisteredException e) {
            Main.log.info("Attempted to invite nations to be allies, but one is not registered.");
        } catch (TooManyInvitesException e) {
            //Too many invites. Oops.
        }
    }

}
