package com.saicone.settings.memory;

import com.saicone.settings.SettingsMemory;
import com.saicone.settings.SettingsNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * Class to save settings nodes into cache instance.<br>
 * Only compatible with Caffeine and Guava cache.
 *
 * @author Rubenicos
 */
public abstract class CacheMemory implements SettingsMemory {

    private static final boolean USE_CAFFEINE;
    private static final boolean USE_GUAVA;

    static {
        boolean useCaffeine = false;
        try {
            Class.forName("com.github.benmanes.caffeine.cache.Cache");
            useCaffeine = true;
        } catch (ClassNotFoundException ignored) { }

        boolean useGuava = false;
        try {
            Class.forName("com.google.common.cache.Cache");
            useGuava = true;
        } catch (ClassNotFoundException ignored) { }

        USE_CAFFEINE = useCaffeine;
        USE_GUAVA = useGuava;
    }

    private final long duration;
    private final TimeUnit unit;

    /**
     * Create a cache memory with the given parameters.
     *
     * @param duration the max duration to save nodes.
     * @param unit     the duration time unit.
     * @return         a cache memory object.
     */
    @NotNull
    public static CacheMemory of(long duration,  @NotNull TimeUnit unit) {
        if (USE_CAFFEINE) {
            return new CaffeineCache(duration, unit);
        }
        if (USE_GUAVA) {
            return new GuavaCache(duration, unit);
        }
        throw new IllegalStateException("The current classpath doesn't contains Caffeine or Guava library");
    }

    /**
     * Constructs a cache memory with given parameters.
     *
     * @param duration the max duration to save nodes.
     * @param unit     the duration time unit.
     */
    public CacheMemory(long duration,  @NotNull TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;
    }

    /**
     * Get the max duration to save nodes.
     *
     * @return a duration number.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Get the duration time unit.
     *
     * @return a time unit.
     */
    @NotNull
    public TimeUnit getUnit() {
        return unit;
    }

    static class CaffeineCache extends CacheMemory {

        private final com.github.benmanes.caffeine.cache.Cache<String, SettingsNode> cache;

        CaffeineCache(long time, @NotNull TimeUnit unit) {
            super(time, unit);
            cache = com.github.benmanes.caffeine.cache.Caffeine.newBuilder().expireAfterAccess(time, unit).build();
        }

        @Override
        public @Nullable SettingsNode get(@NotNull String id) {
            return cache.getIfPresent(id);
        }

        @Override
        public void save(@NotNull String id, @NotNull SettingsNode node) {
            cache.put(id, node);
        }

        @Override
        public void remove(@NotNull String id) {
            cache.invalidate(id);
        }

        @Override
        @SuppressWarnings("all")
        public void remove(@NotNull SettingsNode node) {
            while (cache.asMap().values().remove(node)) {
                /** intentionally empty */
            }
        }

        @Override
        public void clear() {
            cache.invalidateAll();
        }
    }

    static class GuavaCache extends CacheMemory {

        private final com.google.common.cache.Cache<String, SettingsNode> cache;

        GuavaCache(long time, @NotNull TimeUnit unit) {
            super(time, unit);
            cache = com.google.common.cache.CacheBuilder.newBuilder().expireAfterAccess(time, unit).build();
        }

        @Override
        public @Nullable SettingsNode get(@NotNull String id) {
            return cache.getIfPresent(id);
        }

        @Override
        public void save(@NotNull String id, @NotNull SettingsNode node) {
            cache.put(id, node);
        }

        @Override
        public void remove(@NotNull String id) {
            cache.invalidate(id);
        }

        @Override
        @SuppressWarnings("all")
        public void remove(@NotNull SettingsNode node) {
            while (cache.asMap().values().remove(node)) {
                /** intentionally empty */
            }
        }

        @Override
        public void clear() {
            cache.invalidateAll();
        }
    }
}
