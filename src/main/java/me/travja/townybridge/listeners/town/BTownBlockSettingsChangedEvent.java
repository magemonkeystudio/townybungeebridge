package me.travja.townybridge.listeners.town;

import com.palmergames.bukkit.towny.event.TownBlockSettingsChangedEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BTownBlockSettingsChangedEvent implements Listener {

    //TODO Determine what info needs to be synced
    @EventHandler
    public void settingChanged(TownBlockSettingsChangedEvent event) {
        TownBlock block = event.getTownBlock();
        Town town = event.getTown();

//        block.setChanged();
//        block.isChanged();
//
//        block.setLocked();
//        block.isLocked();
//
//        block.setName();
//        block.getName();
//
//        block.setOutpost();
//        block.isOutpost();
//
//        block.setPermissions();
//        block.getPermissions();
//
//        block.setPlotObjectGroup();
//        block.getPlotObjectGroup();
//
//        block.setPlotPrice();
//        block.getPlotPrice();
//
//        block.setResident();
//        block.getResident();
//
//        block.setTown();
//        block.getTown();
//
//        block.setType();
//        block.getType();
//
//        block.setWorld();
//        block.getWorld();
//
//        block.setX();
//        block.getX();
//
//        block.setZ();
//        block.getZ();
    }

}
