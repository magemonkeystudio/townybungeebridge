package me.travja.townybridge.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import me.travja.townybridge.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;

public class JoinListener implements Listener {

    private static ArrayList<String> players = new ArrayList<>();

    @EventHandler
    public void join(PlayerJoinEvent event) {
        if (players.contains(event.getPlayer().getName())) {
            TownyDataSource towny = TownyAPI.getInstance().getDataSource();

            try {
                Town town = towny.getResident(event.getPlayer().getName()).getTown();
                event.getPlayer().teleport(town.getSpawn());
                Main.log.info("Teleported player to spawn of town on join");
            } catch (TownyException e) {
                // Can't really do anything...
            }

        }
    }


    public static void addSpawners(String pl) {
        players.add(pl);
    }

}
