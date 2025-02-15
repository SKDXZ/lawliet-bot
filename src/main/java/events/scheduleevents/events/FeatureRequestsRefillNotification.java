package events.scheduleevents.events;

import java.util.Calendar;
import constants.AssetIds;
import constants.ExceptionRunnable;
import constants.ExternalLinks;
import core.Program;
import core.ShardManager;
import events.scheduleevents.ScheduleEventDaily;
import net.dv8tion.jda.api.entities.Message;

@ScheduleEventDaily
public class FeatureRequestsRefillNotification implements ExceptionRunnable {

    @Override
    public void run() throws Throwable {
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && Program.publicVersion()) {
            String message = "It's the beginning of a new week, therefore you can now boost again for your favorite Lawliet feature requests: " + ExternalLinks.FEATURE_REQUESTS_WEBSITE;
            ShardManager.getLocalGuildById(AssetIds.SUPPORT_SERVER_ID)
                    .map(server -> server.getNewsChannelById(557960859792441357L))
                    .ifPresent(channel -> channel.sendMessage(message).flatMap(Message::crosspost).queue());
        }
    }

}
