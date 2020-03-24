package me.travja.townybridge.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
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
        ArrayList<String> nationAliases = (ArrayList<String>) Bukkit.getPluginCommand("nation").getAliases();

        String[] args = event.getMessage().split(" ");
        String cmd = args[0].replace("/", "").toLowerCase();

        Main.log.info(cmd);

        boolean tCmd = cmd.equals("town") || townAliases.contains(cmd);
        boolean nCmd = cmd.equals("nation") || nationAliases.contains(cmd);

        if (tCmd || nCmd) {
            Main.log.info("We got a towny command!");
            if (args.length <= 1)
                return;
            System.out.println(args[1]);

            if (args[1].equalsIgnoreCase("spawn")) {
                Main.log.info("Spawn command attempted");

                try {
                    String server = "";
                    TownyObject obj = null;
                    if (tCmd)
                        obj = TownyAPI.getInstance().getDataSource().getResident(event.getPlayer().getName()).getTown();
                    else if (nCmd)
                        obj = TownyAPI.getInstance().getDataSource().getResident(event.getPlayer().getName()).getTown().getNation();

                    if (obj == null || BungeeUtil.rightServer(obj)) {
                        Main.log.info("On the right server.");
                        return;
                        //We'll just let Towny handle this.
                    }

                    server = BungeeUtil.getHomeServer(obj);

                    BungeeUtil.connect(event.getPlayer(), server);
                    event.setCancelled(true);
                    Main.log.info("Attempting to send to other server.");
                } catch (NotRegisteredException e) {
                }
            }
        }
    }
}
