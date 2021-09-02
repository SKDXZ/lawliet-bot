package modules;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import commands.runnables.moderationcategory.JailCommand;
import commands.runnables.moderationcategory.UnjailCommand;
import core.CustomObservableMap;
import core.MemberCacheController;
import core.PermissionCheckRuntime;
import modules.schedulers.JailScheduler;
import mysql.modules.jails.DBJails;
import mysql.modules.jails.JailData;
import mysql.modules.moderation.DBModeration;
import mysql.modules.moderation.ModerationData;
import net.dv8tion.jda.api.entities.*;

public class Jail {

    public static boolean jail(Guild guild, Member member, long minutes, String reason) {
        ModerationData moderationData = DBModeration.getInstance().retrieve(guild.getIdLong());
        long muteRoleId = moderationData.getMuteRoleId().orElse(0L);

        CustomObservableMap<Long, JailData> guildJailMap = DBJails.getInstance().retrieve(guild.getIdLong());
        List<Long> currentRoleIds;
        if (guildJailMap.containsKey(member.getIdLong())) {
            currentRoleIds = guildJailMap.get(member.getIdLong()).getPreviousRoleIds();
        } else {
            currentRoleIds = member.getRoles().stream()
                    .map(ISnowflake::getIdLong)
                    .filter(rId -> rId != muteRoleId)
                    .collect(Collectors.toList());
        }

        Instant expiration = minutes > 0 ? Instant.now().plus(Duration.ofMinutes(minutes)) : null;
        JailData jailData = new JailData(guild.getIdLong(), member.getIdLong(), expiration, currentRoleIds);
        guildJailMap.put(member.getIdLong(), jailData);
        JailScheduler.loadJail(jailData);

        List<Role> jailRoles =  moderationData.getJailRoleIds().transform(guild::getRoleById, ISnowflake::getIdLong);
        PermissionCheckRuntime.botCanManageRoles(moderationData.getGuildData().getLocale(), JailCommand.class, jailRoles);
        List<Role> jailRolesAdd = jailRoles.stream()
                .filter(role -> guild.getSelfMember().canInteract(role))
                .collect(Collectors.toList());

        boolean allRolePermissionsFine = member.getRoles().stream()
                .filter(role -> role.getIdLong() != muteRoleId && !jailRolesAdd.contains(role) && member.getRoles().contains(role))
                .allMatch(role -> guild.getSelfMember().canInteract(role));
        List<Role> jailRolesRemove = member.getRoles().stream()
                .filter(role -> role.getIdLong() != muteRoleId && !jailRolesAdd.contains(role) && guild.getSelfMember().canInteract(role))
                .collect(Collectors.toList());

        guild.modifyMemberRoles(member, jailRolesAdd, jailRolesRemove)
                .reason(reason)
                .queue();

        return allRolePermissionsFine;
    }

    public static boolean unjail(Guild guild, User target, String reason) {
        JailData jailData = DBJails.getInstance().retrieve(guild.getIdLong()).remove(target.getIdLong());
        if (jailData != null) {
            Member member = MemberCacheController.getInstance().loadMember(guild, target.getIdLong()).join();
            if (member != null) {
                return unjail(jailData, guild, member, reason);
            }
        }
        return true;
    }

    public static boolean unjail(JailData jailData, Guild guild, Member member, String reason) {
        ModerationData moderationData = DBModeration.getInstance().retrieve(guild.getIdLong());

        boolean allRolePermissionsFine = jailData.getPreviousRoleIds().stream()
                .map(guild::getRoleById)
                .filter(role -> role != null && !member.getRoles().contains(role))
                .allMatch(role -> guild.getSelfMember().canInteract(role));
        List<Role> previousRolesAdd = jailData.getPreviousRoleIds().stream()
                .map(guild::getRoleById)
                .filter(role -> role != null && guild.getSelfMember().canInteract(role))
                .collect(Collectors.toList());

        List<Role> jailRoles =  moderationData.getJailRoleIds().transform(guild::getRoleById, ISnowflake::getIdLong);
        PermissionCheckRuntime.botCanManageRoles(moderationData.getGuildData().getLocale(), UnjailCommand.class, jailRoles);
        List<Role> previousRolesRemove = jailRoles.stream()
                .filter(role -> !previousRolesAdd.contains(role) && guild.getSelfMember().canInteract(role))
                .collect(Collectors.toList());

        guild.modifyMemberRoles(member, previousRolesAdd, previousRolesRemove)
                .reason(reason)
                .queue();

        return allRolePermissionsFine;
    }

}
