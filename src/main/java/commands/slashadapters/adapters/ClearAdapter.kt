package commands.slashadapters.adapters

import commands.runnables.moderationcategory.ClearCommand
import commands.slashadapters.Slash
import commands.slashadapters.SlashAdapter
import commands.slashadapters.SlashMeta
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

@Slash(command = ClearCommand::class)
class ClearAdapter : SlashAdapter() {

    public override fun addOptions(commandData: SlashCommandData): SlashCommandData {
        return commandData
            .addOption(OptionType.INTEGER, "amount", "How many messages shall be removed? (2 - 500)", true)
            .addOption(OptionType.CHANNEL, "channel", "Where do you want to delete the messages?", false)
            .addOption(OptionType.STRING, "members", "Filter by one or more members", false)
    }

    override fun process(event: SlashCommandInteractionEvent): SlashMeta {
        return SlashMeta(ClearCommand::class.java, collectArgs(event))
    }

}