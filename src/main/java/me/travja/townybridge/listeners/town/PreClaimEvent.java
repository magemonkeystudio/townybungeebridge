package me.travja.townybridge.listeners.town;

import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import me.travja.townybridge.util.BungeeUtil;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PreClaimEvent implements Listener {

    @EventHandler
    public void preClaim(TownPreClaimEvent event) {
        Town town = event.getTown();
        TownBlock block = event.getTownBlock();

        if (!BungeeUtil.rightServer(town) && !block.isOutpost()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You must be connected to " + BungeeUtil.getHomeServer(town) + " and the claim must be adjacent to current claims.");
        }
    }

}
