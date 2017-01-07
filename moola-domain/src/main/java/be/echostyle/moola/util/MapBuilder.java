package be.echostyle.moola.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MapBuilder<K,V> {

    private MapBuilder(){};

    private List<Consumer<Map<K,V>>> onBuild = new ArrayList<>();

    public static <K,V> MapBuilder<K,V> map(){
        return new MapBuilder<>();
    }

    public MapBuilder<K,V> entry(K key, V value) {
        onBuild.add(map -> map.put(key, value));
        return this;
    }

    public Map<K,V> linked(){
        LinkedHashMap<K, V> r = new LinkedHashMap<>();
        for (Consumer<Map<K,V>> consumer:onBuild)
            consumer.accept(r);
        return r;
    }
}
