package me.travja.townybridge.util;

public class CacheData {

    private String what;
    private Object obj1, obj2;

    public CacheData(String what, Object obj1, Object obj2) {
        this.what = what;
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    public String getWhat() {
        return what;
    }

    public Object getObj1() {
        return obj1;
    }

    public Object getObj2() {
        return obj2;
    }
}
