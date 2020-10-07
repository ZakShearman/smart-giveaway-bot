package pink.zak.giveawaybot.cache;

import pink.zak.giveawaybot.GiveawayBot;
import pink.zak.giveawaybot.models.Server;
import pink.zak.giveawaybot.service.cache.AccessExpiringCache;

import java.util.concurrent.TimeUnit;

public class ServerCache extends AccessExpiringCache<Long, Server> {

    public ServerCache(GiveawayBot bot) {
        super(bot, bot.getServerStorage(), null, server -> server.getUserCache().invalidateAll(), TimeUnit.MINUTES,  10);
    }

    public void shutdown() {
        for (Server server : this.getMap().values()) {
            server.getUserCache().invalidateAll();
        }
        this.invalidateAll();
    }
}
