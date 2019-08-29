package Commands.BotManagement;

import CommandListeners.onRecievedListener;
import CommandSupporters.Command;
import General.EmbedFactory;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;

import java.time.Duration;
import java.time.Instant;

public class PingCommand extends Command implements onRecievedListener {

    public PingCommand() {
        super();
        trigger = "ping";
        privateUse = false;
        botPermissions = 0;
        userPermissions = 0;
        nsfw = false;
        withLoadingBar = false;
        emoji = "\uD83C\uDFD3";
        thumbnail = "http://icons.iconarchive.com/icons/graphicloads/100-flat-2/128/arrow-refresh-4-icon.png";
        executable = false;
    }

    @Override
    public boolean onRecieved(MessageCreateEvent event, String followedString) throws Throwable {
        Instant startTime = event.getMessage().getCreationTimestamp();
        Message message = event.getServerTextChannel().get().sendMessage(EmbedFactory.getCommandEmbedStandard(this, getString("pong_notime"))).get();
        Instant endTime = Instant.now();

        Duration duration = Duration.between(startTime, endTime);
        message.edit(EmbedFactory.getCommandEmbedStandard(this, getString("pong", String.valueOf((Math.abs(duration.getSeconds()*1000000000) + Math.abs(duration.getNano())) / 1000000)))).get();
        return true;
    }
}
