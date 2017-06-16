package be.echostyle.moola.persistence.cache;

import java.util.function.Supplier;

public interface Cache<K, V> {

    V get(K id, Supplier<V> supplier);
    void put (K id, V value);

}
