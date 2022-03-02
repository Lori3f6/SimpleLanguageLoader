package land.melon.lab.simplelanguageloader.utils;

import java.util.Objects;

/**
 * Util to save a pair of objects as Key-Value
 *
 * @param <K> key
 * @param <V> value
 */
public record Pair<K, V>(K key, V value) {
    /**
     * Shorthand to create a new Pair instance
     * Equals to {@link #Pair(Object, Object)}
     *
     * @param key   key
     * @param value value
     * @param <K>   key type
     * @param <V>   value type
     * @return new Pair instance
     */
    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return key.equals(pair.key) && value.equals(pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
