package modules.repair;

import commands.runnables.utilitycategory.AutoRolesCommand;
import constants.Category;
import constants.FisheryStatus;
import core.MainLogger;
import core.PermissionCheckRuntime;
import core.TextManager;
import mysql.modules.autoroles.AutoRolesBean;
import mysql.modules.autoroles.DBAutoRoles;
import mysql.modules.fisheryusers.DBFishery;
import mysql.modules.fisheryusers.FisheryServerBean;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RolesRepair {

    private static final RolesRepair ourInstance = new RolesRepair();

    public static RolesRepair getInstance() {
        return ourInstance;
    }

    private RolesRepair() {
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void start(JDA jda, int minutes) {
        executorService.submit(() -> run(jda, minutes));
    }

    public void run(JDA jda, int minutes) {
        for (Guild guild : jda.getGuilds()) {
            processAutoRoles(guild, minutes);
            processFisheryRoles(guild, minutes);
        }
    }

    private void processFisheryRoles(Guild guild, int minutes) {
        FisheryServerBean fisheryServerBean = DBFishery.getInstance().retrieve(guild.getIdLong());
        Locale locale = fisheryServerBean.getGuildBean().getLocale();
        if (fisheryServerBean.getGuildBean().getFisheryStatus() != FisheryStatus.STOPPED && fisheryServerBean.getRoleIds().size() > 0) {
            guild.getMembers().stream()
                    .filter(member -> !member.getUser().isBot() && userJoinedRecently(member, minutes))
                    .forEach(member -> checkRoles(locale,
                            TextManager.getString(locale, Category.FISHERY_SETTINGS, "fisheryroles_title"),
                            member,
                            fisheryServerBean.getUserBean(member.getIdLong()).getRoles()
                    ));
        }
    }

    private void processAutoRoles(Guild guild, int minutes) {
        AutoRolesBean autoRolesBean = DBAutoRoles.getInstance().retrieve(guild.getIdLong());
        Locale locale = autoRolesBean.getGuildBean().getLocale();
        if (autoRolesBean.getRoleIds().size() > 0) {
            List<Role> roles = autoRolesBean.getRoleIds().transform(guild::getRoleById, ISnowflake::getIdLong);
            guild.getMembers().stream()
                    .filter(member -> userJoinedRecently(member, minutes))
                    .forEach(member -> checkRoles(locale,
                            TextManager.getString(locale, Category.UTILITY, "autoroles_title"),
                            member,
                            roles
                    ));
        }
    }

    private void checkRoles(Locale locale, String reason, Member member, List<Role> roles) {
        roles.stream()
                .filter(role -> !member.getRoles().contains(role) && PermissionCheckRuntime.getInstance().botCanManageRoles(locale, AutoRolesCommand.class, role))
                .forEach(role -> {
                    MainLogger.get().info("Giving role \"{}\" to user \"{}\" on server \"{}\"", role.getName(), member.getUser().getAsTag(), role.getGuild().getName());
                    role.getGuild().addRoleToMember(member, role).reason(reason).queue();
                });
    }

    private boolean userJoinedRecently(Member member, int minutes) {
        if (member.hasTimeJoined()) {
            return member.getTimeJoined().toInstant().isAfter(Instant.now().minus(minutes, ChronoUnit.MINUTES));
        }
        return false;
    }

}
