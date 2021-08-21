package modules.schedulers;

import java.time.Instant;
import commands.runnables.utilitycategory.ReminderCommand;
import constants.Category;
import core.*;
import core.schedule.MainScheduler;
import core.utils.StringUtil;
import mysql.modules.reminders.DBReminders;
import mysql.modules.reminders.ReminderData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

public class ReminderScheduler extends Startable {

    private static final ReminderScheduler ourInstance = new ReminderScheduler();

    public static ReminderScheduler getInstance() {
        return ourInstance;
    }

    private ReminderScheduler() {
    }

    @Override
    protected void run() {
        try {
            DBReminders.getInstance().retrieveAll()
                    .forEach(this::loadReminderBean);
        } catch (Throwable e) {
            MainLogger.get().error("Could not start reminder", e);
        }
    }

    public void loadReminderBean(ReminderData remindersBean) {
        loadReminderBean(remindersBean.getGuildId(), remindersBean.getId(), remindersBean.getTime());
    }

    public void loadReminderBean(long guildId, long reminderId, Instant due) {
        MainScheduler.getInstance().schedule(due, "reminder_" + reminderId, () -> {
            CustomObservableMap<Long, ReminderData> map = DBReminders.getInstance().retrieve(guildId);
            if (map.containsKey(reminderId) && ShardManager.getInstance().guildIsManaged(guildId)) {
                onReminderDue(map.get(reminderId));
            }
        });
    }

    private void onReminderDue(ReminderData reminderData) {
        DBReminders.getInstance().retrieve(reminderData.getGuildId())
                .remove(reminderData.getId());

        reminderData.getGuild()
                .map(guild -> guild.getTextChannelById(reminderData.getTargetChannelId()))
                .ifPresent(targetChannel -> {
                    if (reminderData.getMessageId() != 0) {
                        TextChannel sourceChannel = targetChannel.getGuild().getTextChannelById(reminderData.getSourceChannelId());
                        if (sourceChannel != null) {
                            sourceChannel.deleteMessageById(reminderData.getMessageId())
                                    .queue(v -> sendReminder(reminderData, targetChannel));
                        }
                    } else {
                        sendReminder(reminderData, targetChannel);
                    }
                });
    }

    private void sendReminder(ReminderData reminderData, TextChannel channel) {
        if (PermissionCheckRuntime.getInstance().botHasPermission(
                reminderData.getGuildData().getLocale(),
                ReminderCommand.class,
                channel,
                Permission.MESSAGE_WRITE
        )) {
            String userMessage = StringUtil.shortenString(reminderData.getMessage(), 1800);
            String message = TextManager.getString(reminderData.getGuildData().getLocale(), Category.UTILITY, "reminder_action", userMessage);
            channel.sendMessage(message)
                    .allowedMentions(null)
                    .queue();
        }
    }

}
