package commands.slashadapters.adapters

import commands.runnables.utilitycategory.RevokeRoleCommand
import commands.slashadapters.Slash
import commands.slashadapters.SlashAdapter
import commands.slashadapters.SlashMeta
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

@Slash(command = RevokeRoleCommand::class)
class RevokeRolesAdapter : SlashAdapter() {

    public override fun addOptions(commandData: SlashCommandData): SlashCommandData {
        return commandData
            .addOption(OptionType.STRING, "role", "Mention one or more roles", true)
    }

    override fun process(event: SlashCommandInteractionEvent): SlashMeta {
        return SlashMeta(RevokeRoleCommand::class.java, collectArgs(event))
    }

}