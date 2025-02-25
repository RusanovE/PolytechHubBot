package org.example.polytechhubbot.telegram

import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.methods.forum.CreateForumTopic
import org.telegram.telegrambots.meta.api.methods.forum.DeleteForumTopic
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

object BotApiMethodBuilder {

    fun sendMessage(
        chatId: String,
        text: String,
        messageThreadId: Int? = null,
        parseMode: String? = null,
        replyMarkup: ReplyKeyboard? = null,
    ): SendMessage {
        return SendMessage().apply {
            this.chatId = chatId
            this.text = text
            this.messageThreadId = messageThreadId
            this.parseMode = parseMode
            this.replyMarkup = replyMarkup
        }
    }

    fun forwardMessage(
        fromChatId: String,
        messageId: Int,
        toChatId: String,
        messageThreadId: Int? = null
    ): ForwardMessage {
        return ForwardMessage().apply {
            this.fromChatId = fromChatId
            this.messageId = messageId
            this.chatId = toChatId
            this.messageThreadId = messageThreadId
        }
    }

    fun deleteForumTopic(
        chatId: String,
        messageThreadId: Int,
    ): DeleteForumTopic {
        return DeleteForumTopic().apply {
            this.chatId = chatId
            this.messageThreadId = messageThreadId
        }
    }

    fun createForumTopic(
        chatId: String,
        name: String,
        iconCustomEmojiId: String? = null
    ): CreateForumTopic {
        return CreateForumTopic().apply {
            this.chatId = chatId
            this.name = name
            this.iconCustomEmojiId = iconCustomEmojiId
        }
    }

    fun createChatInviteLink(
        chatId: String,
        name: String
    ): CreateChatInviteLink{
        return CreateChatInviteLink().apply {
            this.chatId = chatId
            memberLimit = 1
            this.name = name
        }
    }

    fun createInlineKeyboard(buttonsMap: Map<String, String>): InlineKeyboardMarkup {
        val keyboard = buttonsMap.map { (text, callbackData) ->
            listOf(createInlineKeyboardButton(text, callbackData))
        }
        return InlineKeyboardMarkup(keyboard)
    }

    private fun createInlineKeyboardButton(text: String, callbackData: String): InlineKeyboardButton {
        return InlineKeyboardButton().apply {
            this.text = text
            this.callbackData = callbackData
        }
    }

}
