package core;

import constants.AssetIds;
import mysql.modules.guild.DBGuild;
import mysql.modules.stickyroles.DBStickyRoles;
import net.dv8tion.jda.api.utils.ChunkingFilter;

public class ChunkingFilterController implements ChunkingFilter {

    private static final ChunkingFilterController ourInstance = new ChunkingFilterController();

    public static ChunkingFilterController getInstance() {
        return ourInstance;
    }

    private ChunkingFilterController() {
    }

    @Override
    public boolean filter(long guildId) {
        return guildId == AssetIds.SUPPORT_SERVER_ID ||
                guildId == AssetIds.ANICORD_SERVER_ID ||
                DBGuild.getInstance().retrieve(guildId).isBig() ||
                DBStickyRoles.getInstance().retrieve(guildId).getRoleIds().size() > 0;
    }

}
