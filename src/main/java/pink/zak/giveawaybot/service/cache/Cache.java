package pink.zak.giveawaybot.service.cache;

import pink.zak.giveawaybot.GiveawayBot;
import pink.zak.giveawaybot.service.storage.storage.Storage;
import pink.zak.giveawaybot.threads.ThreadFunction;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class Cache<K, V> {
    protected final ConcurrentHashMap<K, V> cacheMap = new ConcurrentHashMap<>();

    protected final ExecutorService executor;
    protected final Consumer<V> removalAction;
    protected final Storage<V> storage;

    public Cache(GiveawayBot bot) {
        this(bot, null, null);
    }

    public Cache(GiveawayBot bot, Storage<V> storage, Consumer<V> removalAction) {
        this.executor = bot.getAsyncExecutor(ThreadFunction.STORAGE);
        this.removalAction = removalAction;
        this.storage = storage;
    }

    public V getSync(K key) {
        V retrieved = this.cacheMap.get(key);
        if (retrieved == null) {
            if (this.storage == null) {
                return null;
            }
            V loaded = this.storage.load(key.toString());
            if (loaded != null) {
                return this.setSync(key, loaded);
            }
            return null;
        }
        return retrieved;
    }

    public CompletableFuture<V> get(K key) {
        return CompletableFuture.supplyAsync(() -> this.getSync(key), this.executor);
    }

    public V setSync(K key, V value) {
        this.cacheMap.put(key, value);
        return value;
    }

    public CompletableFuture<V> set(K key, V value) {
        return CompletableFuture.supplyAsync(() -> this.setSync(key, value), this.executor);
    }

    public boolean contains(K key) {
        return this.cacheMap.containsKey(key);
    }

    public void invalidate(K key) {
        if (this.storage != null) {
            this.storage.save(key.toString(), this.cacheMap.get(key));
        }
        if (this.removalAction != null) {
            this.removalAction.accept(this.cacheMap.get(key));
        }
        this.cacheMap.remove(key);
    }

    public void invalidateAsync(K key) {
        CompletableFuture.runAsync(() -> this.invalidate(key), this.executor);
    }

    public void invalidate(K key, boolean save) {
        if (save) {
            this.invalidate(key);
            return;
        }
        this.cacheMap.remove(key);
    }

    public void invalidateAsync(K key, boolean save) {
        CompletableFuture.runAsync(() -> this.invalidate(key, save), this.executor);
    }

    public void invalidateAll() {
        if (this.storage == null) {
            for (Map.Entry<K, V> entry : this.cacheMap.entrySet()) {
                this.cacheMap.remove(entry.getKey());
            }
            return;
        }
        for (K key : this.cacheMap.keySet()) {
            this.invalidate(key);
        }
    }

    public void invalidateAllAsync() {
        CompletableFuture.runAsync(this::invalidateAll, this.executor);
    }

    public int size() {
        return this.cacheMap.size();
    }

    public ConcurrentHashMap<K, V> getMap() {
        return this.cacheMap;
    }

    public Cache<K, V> getCache() {
        return this;
    }
}
