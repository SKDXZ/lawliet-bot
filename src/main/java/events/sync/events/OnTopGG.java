package events.sync.events;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import core.GlobalThreadPool;
import core.MainLogger;
import events.sync.SyncServerEvent;
import events.sync.SyncServerFunction;
import modules.fishery.Fishery;
import modules.fishery.FisheryStatus;
import mysql.modules.autoclaim.DBAutoClaim;
import mysql.modules.bannedusers.DBBannedUsers;
import mysql.modules.fisheryusers.DBFishery;
import mysql.modules.fisheryusers.FisheryMemberData;
import mysql.modules.guild.DBGuild;
import mysql.modules.upvotes.DBUpvotes;
import mysql.modules.upvotes.UpvoteSlot;
import org.json.JSONObject;

@SyncServerEvent(event = "TOPGG")
public class OnTopGG implements SyncServerFunction {

    @Override
    public JSONObject apply(JSONObject jsonObject) {
        long userId = jsonObject.getLong("user");
        if (DBBannedUsers.getInstance().retrieve().getSlotsMap().containsKey(userId)) {
            return null;
        }

        String type = jsonObject.getString("type");
        boolean isWeekend = jsonObject.has("isWeekend") && jsonObject.getBoolean("isWeekend");

        if (type.equals("upvote")) {
            GlobalThreadPool.getExecutorService().submit(() -> {
                try {
                    processUpvote(userId, isWeekend);
                } catch (ExecutionException | InterruptedException e) {
                    MainLogger.get().error("Exception", e);
                }
            });
        } else {
            MainLogger.get().error("Wrong type: " + type);
        }
        return null;
    }

    protected void processUpvote(long userId, boolean isWeekend) throws ExecutionException, InterruptedException {
        AtomicInteger guilds = new AtomicInteger();
        DBFishery.getInstance().getGuildIdsForFisheryUser(userId).stream()
                .filter(guildId -> DBGuild.getInstance().retrieve(guildId).getFisheryStatus() == FisheryStatus.ACTIVE)
                .forEach(guildId -> {
                    int factor = isWeekend ? 2 : 1;
                    FisheryMemberData userBean = DBFishery.getInstance().retrieve(guildId).getMemberData(userId);

                    if (DBAutoClaim.getInstance().retrieve().isActive(userId)) {
                        userBean.changeValues(Fishery.getClaimValue(userBean) * factor, 0);
                    } else {
                        userBean.addUpvote(factor);
                    }
                    guilds.incrementAndGet();
                });

        DBUpvotes.saveUpvoteSlot(new UpvoteSlot(userId, Instant.now(), 0));
        MainLogger.get().info("UPVOTE | {} ({} guild/s)", userId, guilds.get());
    }

}
