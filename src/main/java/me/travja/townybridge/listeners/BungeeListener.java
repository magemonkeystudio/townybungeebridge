package me.travja.townybridge.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import me.travja.townybridge.Main;
import me.travja.townybridge.listeners.nation.*;
import me.travja.townybridge.listeners.player.BDeletePlayerEvent;
import me.travja.townybridge.listeners.player.BRenameResidentEvent;
import me.travja.townybridge.listeners.town.*;
import me.travja.townybridge.listeners.towny.BTownyTransactionEvent;
import me.travja.townybridge.util.BungeeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class BungeeListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        Main.log.info("Received Message. Channel: " + channel);
        if (!channel.equals(BungeeUtil.CHANNEL))
            return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String one = in.readUTF();
        if (one.equals("SendToSpawn")) {
            String pl = in.readUTF();

            Player p = Bukkit.getPlayer(pl);
            if (p != null && p.isOnline()) {
                TownyDataSource towny = TownyAPI.getInstance().getDataSource();

                try {
                    Town town = towny.getResident(p.getName()).getTown();
                    p.teleport(town.getSpawn());
                    Main.log.info("Teleported player to spawn of town on join");
                } catch (TownyException e) {
                    // Can't really do anything...
                }
            } else
                JoinListener.addSpawners(pl);

            return;
        }


        UUID eventID = UUID.fromString(one); // We really don't need ot do anything with this, aside from sending a response.
        BungeeUtil.sendResponse(eventID);
        String server = in.readUTF();
        Main.log.info("From server: " + server + " (this server: " + Main.server + ")");
        if (server.equals(Main.server))
            return;

        String event = in.readUTF(); // Read the data in the same way you wrote it
        Main.log.info("Event: " + event);

        if (event.equals("DeleteNationEvent")) {
            BDeleteNationEvent.received(in.readUTF());
        } else if (event.equals("NationAddEnemyEvent")) {
            UUID nID = UUID.fromString(in.readUTF());
            UUID eID = UUID.fromString(in.readUTF());

            BNationAddEnemyEvent.received(nID, eID);
        } else if (event.equals("NationAddTownEvent")) {
            UUID nID = UUID.fromString(in.readUTF());
            UUID tID = UUID.fromString(in.readUTF());

            BNationAddTownEvent.received(nID, tID);
        } else if (event.equals("NationInviteTownEvent")) {
            String sname = in.readUTF();
            UUID nID = UUID.fromString(in.readUTF());
            UUID tID = UUID.fromString(in.readUTF());

            BNationInviteTownEvent.received(sname, nID, tID);
        } else if (event.equals("NationRemoveEnemyEvent")) {
            UUID nID = UUID.fromString(in.readUTF());
            UUID eID = UUID.fromString(in.readUTF());

            BNationRemoveEnemyEvent.received(nID, eID);
        } else if (event.equals("NationRemoveTownEvent")) {
            UUID nID = UUID.fromString(in.readUTF());
            UUID tID = UUID.fromString(in.readUTF());

            BNationRemoveTownEvent.received(nID, tID);
        } else if (event.equals("NationRequestAllyNationEvent")) {
            String sname = in.readUTF();
            UUID nID = UUID.fromString(in.readUTF());
            UUID aID = UUID.fromString(in.readUTF());

            BNationRequestAllyNationEvent.received(sname, nID, aID);
        } else if (event.equals("NationTagChangeEvent")) {
            BNationTagChangeEvent.received(UUID.fromString(in.readUTF()), in.readUTF());
        } else if (event.equals("NationTransacionEvent")) {
            BNationTransactionEvent.received(UUID.fromString(in.readUTF()), Integer.parseInt(in.readUTF()), in.readUTF(), in.readUTF());
        } else if (event.equals("NewNationEvent")) {
            String name = in.readUTF();
            UUID id = UUID.fromString(in.readUTF());
            UUID mID = UUID.fromString(in.readUTF());
            String tag = in.readUTF();
            UUID capID = UUID.fromString(in.readUTF());


            BNewNationEvent.received(name, id, mID, tag, capID);
        } else if (event.equals("RenameNationEvent")) {
            UUID id = UUID.fromString(in.readUTF());
            String name = in.readUTF();

            BRenameNationEvent.received(id, name);


        } else if (event.equals("DeletePlayerEvent")) {
            BDeletePlayerEvent.received(in.readUTF());
        } else if (event.equals("RenameResidentEvent")) {
            String old = in.readUTF();
            String name = in.readUTF();

            BRenameResidentEvent.received(old, name);


        } else if (event.equals("DeleteTownEvent")) {
            UUID uuid = UUID.fromString(in.readUTF());
            BDeleteTownEvent.received(uuid);
        } else if (event.equals("NewTownEvent")) {
            String name = in.readUTF();
            UUID id = UUID.fromString(in.readUTF());
            UUID mID = UUID.fromString(in.readUTF());
            String tag = in.readUTF();
            String world = in.readUTF();


            BNewTownEvent.received(name, id, mID, tag, world);
        } else if (event.equals("RenameTownEvent")) {
            UUID id = UUID.fromString(in.readUTF());
            String name = in.readUTF();

            BRenameTownEvent.received(id, name);
        } else if (event.equals("TownAddResidentEvent")) {
            BTownAddResidentEvent.received(UUID.fromString(in.readUTF()), in.readUTF());
        } else if (event.equals("TownAddResidentRankEvent")) {
            BTownAddResidentRankEvent.received(UUID.fromString(in.readUTF()), in.readUTF(), in.readUTF());
        } else if (event.equals("TownBlockSettingsChangedEvent")) {
            //TODO Implement this
        } else if (event.equals("TownInvitePlayerEvent")) {
            BTownInvitePlayerEvent.received(in.readUTF(), UUID.fromString(in.readUTF()), in.readUTF());
        } else if (event.equals("TownRemoveResidentEvent")) {
            BTownRemoveResidentEvent.received(UUID.fromString(in.readUTF()), in.readUTF());
        } else if (event.equals("TownRemoveResidentRankEvent")) {
            BTownRemoveResidentRankEvent.received(UUID.fromString(in.readUTF()), in.readUTF(), in.readUTF());
        } else if (event.equals("TownTagChangeEvent")) {
            BTownTagChangeEvent.received(UUID.fromString(in.readUTF()), in.readUTF());
        } else if (event.equals("TownTransactionEvent")) {
            BTownTransactionEvent.received(UUID.fromString(in.readUTF()), Integer.parseInt(in.readUTF()), in.readUTF(), in.readUTF());
        } else if (event.equals("TownyTransactionEvent")) {
            BTownyTransactionEvent.received(Integer.parseInt(in.readUTF()), in.readUTF(), in.readUTF());
            //TODO War events
        } else {
            Main.getInstance().getLogger().info("Received unknown event: " + event + ". Please contact Travja!");
        }


    }
}
