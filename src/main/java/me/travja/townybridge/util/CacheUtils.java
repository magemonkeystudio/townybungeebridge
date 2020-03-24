package me.travja.townybridge.util;

import me.travja.townybridge.Main;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class CacheUtils {

    private static ArrayList<CacheData> cache = new ArrayList<>();

    public static void addCache(String what, Object obj1, Object obj2, long delayRemove) {
        CacheData data = new CacheData(what, obj1, obj2);
        cache.add(data);

        new BukkitRunnable() {
            public void run() {
                cache.remove(data);
            }
        }.runTaskLater(Main.getInstance(), delayRemove);
    }

    public static void addCache(String what, Object obj, long delayRemove) {
        CacheData data = new CacheData(what, obj, null);
        cache.add(data);

        new BukkitRunnable() {
            public void run() {
                cache.remove(data);
            }
        }.runTaskLater(Main.getInstance(), delayRemove);
    }

    public static void clearCache(String what, Object obj) {
        for (CacheData data : (ArrayList<CacheData>) cache.clone()) {
            if (data.getWhat().equals(what) && data.getObj1().equals(obj))
                cache.remove(data);
        }
    }

    public static boolean checkCache(String what, Object obj) {
        for (CacheData data : cache) {
            if (data.getWhat().equals(what) && data.getObj1().equals(obj))
                return true;
        }

        return false;
    }

    public static boolean checkCache(String what, Object obj1, Object obj2) {
        for (CacheData data : cache) {
            if (data.getWhat().equals(what) && data.getObj1().equals(obj1) && data.getObj2().equals(obj2))
                return true;
        }

        return false;
    }

}
