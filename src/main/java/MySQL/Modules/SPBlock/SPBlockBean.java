package MySQL.Modules.SPBlock;

import Core.CustomObservableList;
import Core.DiscordApiCollection;
import MySQL.BeanWithServer;
import MySQL.Modules.Server.ServerBean;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.javacord.api.entity.server.Server;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Optional;

public class SPBlockBean extends BeanWithServer {

    public enum ActionList { DELETE_MESSAGE, KICK_USER, BAN_USER }

    private boolean active;
    private ActionList action;
    private final CustomObservableList<Long> ignoredUserIds, ignoredChannelIds, logReceiverUserIds;

    public SPBlockBean(ServerBean serverBean, boolean active, ActionList action, @NonNull ArrayList<Long> ignoredUserIds, @NonNull ArrayList<Long> ignoredChannelIds, @NonNull ArrayList<Long> logReceiverUserIds) {
        super(serverBean);
        this.active = active;
        this.action = action;
        this.ignoredUserIds = new CustomObservableList<>(ignoredUserIds);
        this.logReceiverUserIds = new CustomObservableList<>(logReceiverUserIds);
        this.ignoredChannelIds = new CustomObservableList<>(ignoredChannelIds);
    }


    /* Getters */

    public CustomObservableList<Long> getIgnoredUserIds() {
        return ignoredUserIds;
    }

    public CustomObservableList<Long> getLogReceiverUserIds() {
        return logReceiverUserIds;
    }

    public CustomObservableList<Long> getIgnoredChannelIds() { return ignoredChannelIds; }

    public boolean isActive() {
        return active;
    }

    public ActionList getAction() { return action; }


    /* Setters */

    public void toggleActive() {
        this.active = !this.active;
        setChanged();
        notifyObservers();
    }

    public void setAction(ActionList action) {
        if (this.action != action) {
            this.action = action;
            setChanged();
            notifyObservers();
        }
    }
}
