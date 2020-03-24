package me.travja.townybridge.util;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import me.travja.townybridge.Main;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.UUID;

public class BungeeUtil {

    public static final String CHANNEL = "travja:townybridge";
    private static ArrayList<ByteArrayDataOutput> queued = new ArrayList<>();
    private static boolean queueRunning = false;

    static {
        queue();
    }

    public static void sendMessage(String event, String... data) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(UUID.randomUUID().toString());
        out.writeUTF(Main.server);
        out.writeUTF(event);
        for (String dat : data)
            out.writeUTF(dat);

        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

        if (player == null) { //QUEUE
            queued.add(out);
            queue();
            return;
        }

        Main.log.info("Sending message: " + event + " -- " + StringUtils.join(data, ", "));
        player.sendPluginMessage(Main.getInstance(), CHANNEL, out.toByteArray());
    }

    public static void sendResponse(UUID id) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(id.toString());
        out.writeUTF("TownyReceived");

        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

        if (player == null) { //QUEUE
            queued.add(out);
            queue();
            return;
        }

        Main.log.info("Sending response: " + id.toString());
        player.sendPluginMessage(Main.getInstance(), CHANNEL, out.toByteArray());
    }

    private static void sendFirstQueue() {
        if (queued.size() <= 0)
            return;

        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        ByteArrayDataOutput out = queued.get(0);

        if (player == null)
            return;


        player.sendPluginMessage(Main.getInstance(), CHANNEL, out.toByteArray());
        queued.remove(out);
    }

    private static void queue() {
        if (queueRunning)
            return;

        new BukkitRunnable() {
            public void run() {
                if (Bukkit.getOnlinePlayers().size() == 0)
                    return;

                while (queued.size() > 0) {
                    sendFirstQueue();
                }

                if (queued.size() == 0) {
                    queueRunning = false;
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20L, 20L);
    }

    public static void connect(Player player, String server, boolean nation) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Spawn" + (nation ? "N" : ""));
        out.writeUTF(server);

        player.sendPluginMessage(Main.getInstance(), CHANNEL, out.toByteArray());
    }


    public static void sendBlocks(Town town) {
        TownyDataSource towny = TownyAPI.getInstance().getDataSource();
        for (TownBlock block : town.getTownBlocks()) {
            int x = block.getX();
            int z = block.getZ();
            TownyWorld world = block.getWorld();
            String worldName = world.getName();
        }
    }



    public static boolean rightServer(TownyObject town) {
        String server = getHomeServer(town);

        return server.equals("") || server.equals(Main.server);
    }

    public static String getHomeServer(TownyObject town) {
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
