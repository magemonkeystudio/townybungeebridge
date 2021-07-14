package townybungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public class BungeeListener implements Listener {

    private Logger log;
    private static HashMap<String, MessageData> cached = new HashMap<>();
    private static ArrayList<String> recent = new ArrayList<>();

    private static ArrayList<String> sync = new ArrayList<>();

    {
        log = Bridge.getInstance().getLogger();
        Configuration config = Bridge.getConfig();
        Configuration cache = config.getSection("cache");
        for (String key : cache.getKeys()) {
            Configuration section = cache.getSection(key);
            MessageData data = new MessageData(section.getString("server"), section.getStringList("data").toArray(new String[0]));
            cached.put(key, data);


            for (ServerInfo info : Bridge.getInstance().getProxy().getServers().values()) {
                if (info.getName().equals(data.getServer()))
                    continue;

                info.sendData(Bridge.CHANNEL, data.toByteArray(), true);
                log.info("Sending cached data to " + info.getName());
            }
        }

        sync = (ArrayList<String>) config.getStringList("sync");

        config.set("cache", null);
        Bridge.saveConfig();
    }

    public static void save() {
        Configuration config = Bridge.getConfig();
        for (String key : cached.keySet()) {
            config.set("cache." + key + ".server", cached.get(key).getServer());
            config.set("cache." + key + ".data", cached.get(key).getData());
        }
        Bridge.saveConfig();
    }

    @EventHandler
    public void send(PluginMessageEvent event) {
        Connection sender = event.getSender();
        Connection receiver = event.getReceiver();
        byte[] data = event.getData();
        String tag = event.getTag();

        if (!tag.equals(Bridge.CHANNEL))
            return;

        String dat = "";

        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        byte[] msgbytes = new byte[data.length];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
        String read;
        try {
            String idstr = msgin.readUTF();

            if (idstr.equals("Spawn")) {
                String conn = msgin.readUTF();
                ServerInfo dest = Bridge.getInstance().getProxy().getServerInfo(conn);
                log.info("Attempting to send " + receiver.toString() + " to " + conn);
                Bridge.getInstance().getProxy().getPlayer(receiver.toString()).connect(dest);

                ByteArrayDataOutput out = ByteStreams.newDataOutput();

                out.writeUTF("SendTo" + idstr);
                out.writeUTF(receiver.toString());
                dest.sendData(Bridge.CHANNEL, out.toByteArray());
                return;
            }


            UUID id = UUID.fromString(idstr);
            String server = msgin.readUTF();

            if (server.equals("TownyReceived")) {
                String key = id.toString() + "~" + ((Server) sender).getInfo().getName();
                ArrayList<String> copy = (ArrayList<String>) cached.get(key).getData().clone();
                copy.remove(0);//Remove the id and the server.
                copy.remove(0);
                String str = join(copy);

                recent.add(str);
                System.out.println("Added recent: " + str);
                removeDelay(str);
                cached.remove(key);
                System.out.println("Removed cache for " + key);
            } else {
                ArrayList<String> messages = new ArrayList<>();
                messages.add(idstr);
                messages.add(server);

                log.info("Received message on TownyBridge subchannel, server: " + server);
                while (msgin.available() > 0) {
                    read = msgin.readUTF();
                    messages.add(read);
                    dat += read + ", ";
                }

                if (dat.length() >= 2) {
                    dat = dat.substring(0, dat.lastIndexOf(","));
                }


                log.info("Sender: " + server + ", Receiver: " + receiver.toString() + ", Tag: " + tag + ", Data: " + dat);
                if(!recent.contains(dat)) { //If this data hasn't come through in the past 0.5 seconds, we'll send it on.
                    MessageData msgdata = new MessageData(server, messages.toArray(new String[0]));
                    for(String serv: sync) {
                        ServerInfo info = Bridge.getInstance().getProxy().getServerInfo(serv);
                        if(info == null)
                            continue;
//                    for (ServerInfo info : Bridge.getInstance().getProxy().getServers().values()) {
                        if (info.getName().equals(((Server) sender).getInfo().getName()))
                            continue;

                        info.sendData(Bridge.CHANNEL, msgdata.toByteArray(), true);
                        log.info("Forwarding data to " + info.getName() + " (" + dat + ")");
                        recent.add(dat);
                        removeDelay(dat);
                        cached.put(id.toString() + "~" + info.getName(), msgdata);
                        log.info("Cached values for server " + info.getName());
                    }
                } else {
                    log.info("Received duplicate data. Not fowarding. (" + dat + ")");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String join(ArrayList<String> list) {
        String ret = "";
        for (String str : list) {
            ret += str + ", ";
        }

        return ret.substring(0, ret.lastIndexOf(","));
    }

    private void removeDelay(String cache) {
        new Thread() {
            @Override
            public void run() {
                try {
                    this.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                recent.remove(cache);
            }
        }.start();
    }

}
