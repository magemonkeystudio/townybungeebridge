package me.travja.townybridge.util;

import java.util.ArrayList;
import java.util.Arrays;

public class TranslatedType {

    private ArrayList<Class> objs = new ArrayList<>();

    public TranslatedType(Class ... objects) {
        objs.addAll(Arrays.asList(objects));
    }

    public Class getObject(int index) {
        return objs.get(index);
    }

    public int num() {
        return objs.size();
    }

}
