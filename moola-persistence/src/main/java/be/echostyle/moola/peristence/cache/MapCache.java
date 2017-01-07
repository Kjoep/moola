package be.echostyle.moola.peristence.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MapCache<K, V> implements Cache<K, V> {

    private Map<K, V> map = new HashMap<K, V>();

    @Override
    public V get(K id, Supplier<V> supplier) {
        V r = map.get(id);
        if (r==null) {
            r = supplier.get();
            map.put(id, r);
        }
        return r;
    }

    @Override
    public void put(K id, V value) {
        map.put(id, value);
    }
}
