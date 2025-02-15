package commands

import core.slashmessageaction.SlashAckSendMessageAction
import core.slashmessageaction.SlashHookSendMessageAction
import core.utils.JDAUtil
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.channel.GenericChannelEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction

class CommandEvent : GenericChannelEvent {

    val member: Member
    val slashCommandInteractionEvent: SlashCommandInteractionEvent?
    val messageReceivedEvent: MessageReceivedEvent?
    val user: User
        get() = member.user
    val repliedMember: Member?
        get() = messageReceivedEvent?.message?.messageReference?.message?.member
    val textChannel: TextChannel
        get() {
            val channel = getChannel()
            if (channel is TextChannel) {
                return channel
            }
            throw IllegalStateException("Cannot convert channel of type $channelType to TextChannel")
        }
    val guildMessageChannel: GuildMessageChannel
        get() {
            val channel = getChannel()
            if (channel is GuildMessageChannel) {
                return channel
            }
            throw IllegalStateException("Cannot convert channel of type $channelType to GuildMessageChannel")
        }
    var ack = false;

    constructor(event: SlashCommandInteractionEvent) : super(event.jda, event.responseNumber, event.channel) {
        slashCommandInteractionEvent = event
        messageReceivedEvent = null
        member = event.member!!
    }

    constructor(event: MessageReceivedEvent) : super(event.jda, event.responseNumber, event.channel) {
        slashCommandInteractionEvent = null
        messageReceivedEvent = event
        member = event.member!!
    }

    fun isSlashCommandInteractionEvent(): Boolean {
        return slashCommandInteractionEvent != null
    }

    fun isMessageReceivedEvent(): Boolean {
        return messageReceivedEvent != null
    }

    fun replyMessage(content: String): MessageCreateAction {
        if (isMessageReceivedEvent()) {
            return JDAUtil.replyMessage(messageReceivedEvent!!.message, content)
        } else {
            if (slashCommandInteractionEvent!!.isAcknowledged) {
                return SlashHookSendMessageAction(slashCommandInteractionEvent.hook.sendMessage(content))
            } else {
                return SlashAckSendMessageAction(slashCommandInteractionEvent.reply(content))
            }
        }
    }

    fun replyMessageEmbeds(embed: MessageEmbed, vararg embeds: MessageEmbed): MessageCreateAction {
        val fullEmbeds = arrayOf(embed).plus(embeds);
        return replyMessageEmbeds(fullEmbeds.toList())
    }

    fun replyMessageEmbeds(embeds: Collection<MessageEmbed>): MessageCreateAction {
        if (isMessageReceivedEvent()) {
            return JDAUtil.replyMessageEmbeds(messageReceivedEvent!!.message, embeds)
        } else {
            if (slashCommandInteractionEvent!!.isAcknowledged) {
                return SlashHookSendMessageAction(slashCommandInteractionEvent.hook.sendMessageEmbeds(embeds))
            } else {
                return SlashAckSendMessageAction(slashCommandInteractionEvent.replyEmbeds(embeds))
            }
        }
    }

    fun deferReply() {
        if (ack) {
            return
        }

        ack = true
        slashCommandInteractionEvent?.let {
            if (!it.isAcknowledged) {
                it.deferReply().queue()
            }
        }
        messageReceivedEvent?.channel?.asTextChannel()?.sendTyping()?.queue()
    }

}