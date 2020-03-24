package me.travja.townybridge.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import me.travja.townybridge.Main;
import me.travja.townybridge.util.BungeeUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;

public class CmdListener implements Listener {

    @EventHandler
    public void cmd(PlayerCommandPreprocessEvent event) {
        ArrayList<String> townAliases = (ArrayList<String>) Bukkit.getPluginCommand("town").getAliases();

        String[] args = event.getMessage().split(" ");
        String cmd = args[0].replace("/", "").toLowerCase();

        Main.log.info(cmd);

        if (cmd.equals("town") || townAliases.contains(cmd)) {
            Main.log.info("We got a towny command!");
            if (args.length <= 1)
                return;
            System.out.println(args[1]);

            if (args[1].equalsIgnoreCase("spawn")) {
                Main.log.info("Spawn command attempted");

                try {
                    Town town = TownyAPI.getInstance().getDataSource().getResident(event.getPlayer().getName()).getTown();
                    if (town == null || rightServer(town)) {
                        Main.log.info("On the right server.");
                        return;
                        //We'll just let Towny handle this.
                    }

                    String server = getHomeServer(town);

                    BungeeUtil.connect(event.getPlayer(), server);
                    event.setCancelled(true);
                    Main.log.info("Attempting to send to other server.");
                } catch (NotRegisteredException e) {
                }
            }
        }
    }

    private boolean rightServer(Town town) {
        String server = getHomeServer(town);

        return server.equals("") || server.equals(Main.server);
    }

    private String getHomeServer(Town town) {
        String server = "";
        Main.log.info("Has meta? " + town.hasMeta());
        if (!town.hasMeta())
            return server;

        for (CustomDataField<?> data : town.getMetadata()) {
            if (data.getKey().equals("homeserver")) {
                server = (String) data.getValue();
                break;
            }
        }
        Main.log.info("Home Server: " + server);
        return server;
    }
}
