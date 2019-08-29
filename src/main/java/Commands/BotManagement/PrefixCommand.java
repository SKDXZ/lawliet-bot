package Commands.BotManagement;

import CommandListeners.onRecievedListener;
import CommandSupporters.Command;
import Constants.Permission;
import General.EmbedFactory;
import General.TextManager;
import General.Tools;
import MySQL.DBServer;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;

public class PrefixCommand extends Command implements onRecievedListener {
    private static ArrayList<Integer> picked = new ArrayList<>();

    public PrefixCommand() {
        super();
        trigger = "prefix";
        privateUse = false;
        botPermissions = 0;
        userPermissions = Permission.MANAGE_SERVER;
        nsfw = false;
        withLoadingBar = false;
        thumbnail = "http://icons.iconarchive.com/icons/graphicloads/flat-finance/128/name-card-icon.png";
        emoji = "\uD83D\uDCDB";
        executable = false;
    }

    @Override
    public boolean onRecieved(MessageCreateEvent event, String followedString) throws Throwable {
        Server server = event.getServer().get();
        if (followedString.length() > 0) {
            if (followedString.length() <= 5) {
                DBServer.setPrefix(server, followedString);

                if (server.canYouChangeOwnNickname()) {
                    String nickname = Tools.cutSpaces(server.getDisplayName(event.getApi().getYourself()));
                    String[] nicknameArray = nickname.split("\\[");

                    if (nicknameArray.length == 1) {
                        server.updateNickname(event.getApi().getYourself(), nickname + " [" + followedString + "]");
                    } else if (nicknameArray.length == 2 && nicknameArray[1].contains("]")) {
                        server.updateNickname(event.getApi().getYourself(), Tools.cutSpaces(nicknameArray[0]) + " [" + followedString + "]");
                    }
                }

                event.getChannel().sendMessage(EmbedFactory.getCommandEmbedSuccess(this, getString("changed", followedString))).get();
                return true;
            } else {
                event.getChannel().sendMessage(EmbedFactory.getCommandEmbedError(this,
                        TextManager.getString(locale, TextManager.GENERAL, "args_too_long", "5"))).get();
                return false;
            }
        } else {
            event.getChannel().sendMessage(EmbedFactory.getCommandEmbedError(this,
                    getString("no_arg"))).get();
            return false;
        }
    }
}
