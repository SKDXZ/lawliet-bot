package events.discordevents.guildmessagereceived;

import events.discordevents.DiscordEvent;
import events.discordevents.EventPriority;
import events.discordevents.eventtypeabstracts.GuildMessageReceivedAbstract;
import modules.automod.InviteFilter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@DiscordEvent(priority = EventPriority.HIGH, allowBannedUser = true)
public class GuildMessageReceivedInviteFilter extends GuildMessageReceivedAbstract {

    @Override
    public boolean onGuildMessageReceived(MessageReceivedEvent event) throws Throwable {
        return new InviteFilter(event.getMessage()).check();
    }

}
