package land.melon.lab.simplelanguageloader.utils;

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
}
