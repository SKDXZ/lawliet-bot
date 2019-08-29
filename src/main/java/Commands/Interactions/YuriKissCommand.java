package Commands.Interactions;
import CommandListeners.onRecievedListener;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;

public class YuriKissCommand extends InteractionCommand implements onRecievedListener {
    private static ArrayList<Integer> picked = new ArrayList<>();

    public YuriKissCommand() {
        super();
        trigger = "yurikiss";
        emoji = "\uD83D\uDC69\u200D❤️\u200D\uD83D\uDC69";
        nsfw = false;
        gifs = new String[]{
                "https://i.gifer.com/Djbt.gif",
                "https://i.gifer.com/Djbt.gif",
                "https://i.gifer.com/KTGr.gif",
                "https://i.gifer.com/J1b0.gif",
                "https://i.gifer.com/HAnw.gif",
                "https://www.wykop.pl/cdn/c3201142/comment_tfROJ3JwtatzGcJxpnnFRiunICfsZsb5.gif",
                "https://media1.tenor.com/images/279c4716a469ace39b15e34d7fa3e7c4/tenor.gif?itemid=11487318",
                "https://data.whicdn.com/images/95252800/original.gif",
                "https://cdn.weeb.sh/images/rJrCj6_w-.gif",
                "https://cdn.discordapp.com/attachments/499629904380297226/579494706870747147/0.gif",
                "https://cdn.discordapp.com/attachments/499629904380297226/579494710247292929/1.gif"
        };
    }

    @Override
    public boolean onRecieved(MessageCreateEvent event, String followedString) throws Throwable {
        return onInteractionRecieved(event, followedString, picked);
    }
}
