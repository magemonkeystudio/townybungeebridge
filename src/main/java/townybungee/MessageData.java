package townybungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.io.Serializable;
import java.util.ArrayList;

public class MessageData implements Serializable {

    private String server = "";
    private ArrayList<String> data = new ArrayList<>();

    public MessageData(String server, String... data) {
        this.server = server;
        for (String dat : data) {
            this.data.add(dat);
        }
    }

    public ArrayList<String> getData() {
        return data;
    }

    public String getServer() {
        return server;
    }

    public byte[] toByteArray() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        for (String dat : data) {
            out.writeUTF(dat);
        }

        return out.toByteArray();
    }

}
