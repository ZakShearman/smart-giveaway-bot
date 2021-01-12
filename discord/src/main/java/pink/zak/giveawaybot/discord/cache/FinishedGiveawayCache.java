package pink.zak.giveawaybot.discord.cache;

import com.google.common.collect.Lists;
import pink.zak.giveawaybot.discord.GiveawayBot;
import pink.zak.giveawaybot.discord.models.Server;
import pink.zak.giveawaybot.discord.models.giveaway.finished.FullFinishedGiveaway;
import pink.zak.giveawaybot.discord.models.giveaway.finished.PartialFinishedGiveaway;
import pink.zak.giveawaybot.discord.service.cache.caches.AccessExpiringCache;
import pink.zak.giveawaybot.discord.storage.finishedgiveaway.FullFinishedGiveawayStorage;
import pink.zak.giveawaybot.discord.storage.finishedgiveaway.PartialFinishedGiveawayStorage;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class FinishedGiveawayCache extends AccessExpiringCache<Long, FullFinishedGiveaway> {
    private final AccessExpiringCache<Long, PartialFinishedGiveaway> partialCache;
    private final PartialFinishedGiveawayStorage partialStorage;

    public FinishedGiveawayCache(GiveawayBot bot) {
        super(bot, bot.getFinishedGiveawayStorage(), TimeUnit.MINUTES, 10);

        this.partialStorage = new PartialFinishedGiveawayStorage(bot);
        this.partialCache = new AccessExpiringCache<>(bot, this.partialStorage, TimeUnit.HOURS, 1);
    }

    @Override
    public FullFinishedGiveaway set(Long key, FullFinishedGiveaway value) {
        return this.set(key, value, true);
    }

    public FullFinishedGiveaway set(Long key, FullFinishedGiveaway value, boolean save) {
        super.set(key, value);
        if (save) {
            this.storage.save(value);
        }
        return value;
    }

    @Override
    public FullFinishedGiveaway get(Long key) {
        this.partialCache.invalidate(key, false);
        return super.get(key);
    }

    public List<FullFinishedGiveaway> getAll(Server server) {
        List<Long> remainingIds = Lists.newArrayList();
        List<FullFinishedGiveaway> giveaways = Lists.newArrayList();

        for (long giveawayId : server.getFinishedGiveaways()) {
            if (this.contains(giveawayId)) {
                giveaways.add(this.get(giveawayId));
            } else {
                remainingIds.add(giveawayId);
            }
        }
        if (remainingIds.isEmpty()) {
            return giveaways;
        }
        List<FullFinishedGiveaway> loadedGiveaways = ((FullFinishedGiveawayStorage) this.storage).loadAll(server, remainingIds);
        for (FullFinishedGiveaway giveaway : loadedGiveaways) {
            this.set(giveaway.getMessageId(), giveaway, false);
        }
        giveaways.addAll(loadedGiveaways);
        return giveaways;
    }

    public List<PartialFinishedGiveaway> getAllPartial(Server server) {
        long startA = System.currentTimeMillis();
        List<Long> remainingIds = Lists.newArrayList();
        List<PartialFinishedGiveaway> giveaways = Lists.newArrayList();

        for (long giveawayId : server.getFinishedGiveaways()) {
            if (this.contains(giveawayId)) {
                giveaways.add(this.get(giveawayId));
            } else if (this.partialCache.contains(giveawayId)) {
                giveaways.add(this.partialCache.get(giveawayId));
            } else {
                remainingIds.add(giveawayId);
            }
        }
        if (remainingIds.isEmpty()) {
            GiveawayBot.logger().info("Took {}ms to do A", System.currentTimeMillis() - startA);
            return giveaways;
        }
        long startB = System.currentTimeMillis();
        List<PartialFinishedGiveaway> loadedGiveaways = this.partialStorage.loadAll(server, remainingIds);
        GiveawayBot.logger().info("Took {}ms to do B", System.currentTimeMillis() - startB);
        for (PartialFinishedGiveaway giveaway : loadedGiveaways) {
            this.partialCache.set(giveaway.getMessageId(), giveaway);
        }
        giveaways.addAll(loadedGiveaways);
        GiveawayBot.logger().info("Took {}ms to do A", System.currentTimeMillis() - startA);
        return giveaways;
    }
}
