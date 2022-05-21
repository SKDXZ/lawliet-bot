package commands.slashadapters

import commands.Category
import commands.Command
import commands.CommandContainer
import constants.Language
import core.TextManager
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import okhttp3.internal.toImmutableList
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

abstract class SlashAdapter {

    protected abstract fun addOptions(commandData: SlashCommandData): SlashCommandData

    abstract fun process(event: SlashCommandInteractionEvent): SlashMeta

    fun name(): String {
        val slash = javaClass.getAnnotation(Slash::class.java)
        var name = slash.name
        if (name.isEmpty()) {
            name = Command.getCommandProperties(slash.command).trigger
        }
        return name
    }

    fun description(): String {
        val slash = javaClass.getAnnotation(Slash::class.java)
        var description = slash.description
        if (description.isEmpty()) {
            val trigger = name()
            val clazz = CommandContainer.getCommandMap()[trigger]!!
            val category = Command.getCategory(clazz)
            description = TextManager.getString(Language.EN.locale, category, trigger + "_description")
        }
        return description
    }

    fun commandClass(): KClass<out Command> {
        val slash = javaClass.getAnnotation(Slash::class.java)
        return slash.command
    }

    fun commandCategories(): Array<Category> {
        val slash = javaClass.getAnnotation(Slash::class.java)
        return slash.commandCategories
    }

    fun messageCommandAssociations(): List<String> {
        val list = ArrayList<String>()

        val commandClass = commandClass()
        if (commandClass != Command::class.java) {
            val trigger = Command.getCommandProperties(commandClass).trigger
            list += trigger
        }
        list += commandCategories()
            .map { it.id }

        return list.toImmutableList()
    }

    fun generateCommandData(): SlashCommandData {
        val commandData = Commands.slash(name(), description())
        return addOptions(commandData)
    }

    open fun retrieveChoices(event: CommandAutoCompleteInteractionEvent): List<net.dv8tion.jda.api.interactions.commands.Command.Choice> {
        return emptyList()
    }

    companion object {

        @JvmStatic
        protected fun collectArgs(event: SlashCommandInteractionEvent, vararg exceptions: String): String {
            val argsBuilder = StringBuilder()
            for (option in event.options) {
                if (Arrays.stream(exceptions).noneMatch { exception: String -> option.name == exception }) {
                    when (option.type) {
                        OptionType.BOOLEAN -> argsBuilder.append(option.name).append(" ")
                        OptionType.ATTACHMENT -> argsBuilder.append(option.asAttachment.url).append(" ")
                        else -> argsBuilder.append(option.asString).append(" ")
                    }
                }
            }
            return argsBuilder.toString()
        }

    }
}