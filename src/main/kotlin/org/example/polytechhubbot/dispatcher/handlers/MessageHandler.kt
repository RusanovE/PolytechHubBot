package org.example.polytechhubbot.dispatcher.handlers

import mu.KotlinLogging
import org.example.polytechhubbot.enums.Emoji
import org.example.polytechhubbot.enums.UpdateType
import org.example.polytechhubbot.enums.UserType
import org.example.polytechhubbot.service.ForumTopicService
import org.example.polytechhubbot.service.LocalizationService
import org.example.polytechhubbot.telegram.BotApiMethodBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*

@Component
class MessageHandler(val topicService: ForumTopicService, val localizationService: LocalizationService): AbstractHandler {

    private val log = KotlinLogging.logger {}

    @Value("\${adminForum.id}")
    var forumId: Long = 0


    override fun canHandle(updateType: UpdateType): Boolean {
        return updateType.toString().endsWith("FORUM")
                || updateType == UpdateType.CREATED_NEW_TOPIC
                || updateType == UpdateType.CLOSED_TOPIC
    }

    override suspend fun answer(updateType: UpdateType, userType: UserType, update: Update): List<BotApiMethod<*>> {
        val userLanguage = Locale.forLanguageTag(update.message.from.languageCode ?: "en")

        return when (updateType to userType) {
            UpdateType.TO_FORUM to UserType.PERSON -> handleToForum(update.message, userLanguage)
            UpdateType.FROM_FORUM to UserType.FROM_GROUP -> handleFromForum(update.message, userLanguage)
            UpdateType.CREATED_NEW_TOPIC to UserType.FROM_GROUP -> handleCreatedNewTopic(update.message, userLanguage)
            UpdateType.CLOSED_TOPIC to UserType.FROM_GROUP -> handleClosedTopic(update.message, userLanguage)
            UpdateType.UNKNOWN to UserType.FROM_CHANNEL -> {
                log.warn { "Unknown message type: $updateType, from $userType" }
                emptyList()
            }
            else -> emptyList()
        }
    }

    private fun handleFromForum(message: Message, locale: Locale): List<BotApiMethod<*>> {
        val chatId = message.chatId.toString()
        if (message.replyToMessage.text == null) {
            val text = localizationService.getMessage("message.reply.required", locale)
            return listOf(BotApiMethodBuilder.sendMessage(
                chatId,
                text = text,
                message.messageThreadId)
            )

        }else{
            val originalMessageChatId = topicService.findTopicByThreadId(message.messageThreadId.toString())?.chatId.toString()
            log.info { "The response from the admins will be sent to the user with chatId: $originalMessageChatId" }

            val sendMessage1 = BotApiMethodBuilder.sendMessage(
                originalMessageChatId,
                text = message.text,
                )

            val textForAdmin = localizationService.getMessage("message.response.sent", locale, Emoji.DRAGON.value)
            val sendMessage2 = BotApiMethodBuilder.sendMessage(
                chatId,
                text = textForAdmin,
                message.messageThreadId
            )
            return listOf(sendMessage1,sendMessage2)
        }
    }

    private fun handleToForum(message: Message, locale: Locale):List<BotApiMethod<*>> {
        val chatId = message.chatId
        val senderUsername = message.from.userName ?: message.from.firstName

        log.info { "User $senderUsername ($chatId) ask the question: ${message.text}" }

        try {
            val forumTopic = topicService.findTopicByChatId(chatId)
                ?: throw IllegalStateException("Topic for user $chatId not found")

            log.info { "The message will be forwarded to the topic: ${forumTopic.topicThreadId}" }

            val forwardMessage = BotApiMethodBuilder.forwardMessage(
                chatId.toString(),
                message.messageId,
                forumId.toString(),
                forumTopic.topicThreadId.toInt()
            )

            val text = localizationService.getMessage("message.forward.success", locale, Emoji.COWBOY.value)
            val sendMessage = BotApiMethodBuilder.sendMessage(
                chatId.toString(),
                text = text
            )

            return listOf(forwardMessage,sendMessage)
        }catch (e: Exception){
            log.error("User ${message.from.firstName} tried ask the question, but unsuccessfully.\nHis message is: ${message.text}\n\nThe Problem is: ${e.message}")
            return listOf(BotApiMethodBuilder.sendMessage(
                chatId.toString(),
                text = localizationService.getMessage("message.forward.error", locale, e.message.toString()) )
            )
        }
    }

    private fun handleClosedTopic(message: Message, locale: Locale): List<BotApiMethod<*>> {
        val messageThreadId = message.messageThreadId

        try {
            val topic = topicService.findTopicByThreadId(messageThreadId.toString())
                ?: throw IllegalStateException("Topic with ID $messageThreadId not found.")

            val userChatId = topic.chatId.toString()
            log.info { "Notifying user about closing topic for user with chatId: $userChatId" }
            val userNotificationText = localizationService.getMessage("message.topic.closed", locale)
            val notifyMessage = BotApiMethodBuilder.sendMessage(
                userChatId,
                text = userNotificationText
            )

            topicService.deleteTopicByThreadId(messageThreadId.toString())
            log.info { "Information about the topic with ID $messageThreadId has been removed from the database." }

            val deleteForumTopic = BotApiMethodBuilder.deleteForumTopic(
                forumId.toString(),
                messageThreadId
            )
            log.info { "The topic with ID $messageThreadId has been successfully removed from the forum." }

            return listOf(notifyMessage,deleteForumTopic)

        } catch (e: IllegalStateException) {
            log.warn { "Error closing topic: ${e.message}" }
            val errorText = localizationService.getMessage("message.topic.close.error", locale, e.message.toString())
            return listOf(BotApiMethodBuilder.sendMessage(
                message.chatId.toString(),
                text = errorText)
            )
        } catch (e: Exception) {
            log.error(e) { "An unexpected error occurred while processing topic closure." }
            val errorText = localizationService.getMessage("message.topic.close.unknown", locale, e.message.toString())
            return listOf(BotApiMethodBuilder.sendMessage(
                message.chatId.toString(),
                text = errorText)
            )
        }
    }

    private fun handleCreatedNewTopic(message: Message, locale: Locale): List<BotApiMethod<*>> {

        try {
            val topicTitle = message.forumTopicCreated?.name
                ?: throw IllegalStateException("User topic assembly failed. Something went wrong")

            val updateForumTopic = topicService.updateForumTopic(topicTitle, message.messageThreadId)
            log.info { "New topic successfully created: ${updateForumTopic?.topicTitle}" }

            val text = localizationService.getMessage("message.topic.create.welcome", locale, Emoji.IDOL.value, Emoji.RAT.value.toString(), Emoji.BOT_FACE.value)
            return listOf(BotApiMethodBuilder.sendMessage(
                updateForumTopic?.chatId.toString(),
                text = text)
            )
        }catch (e:Exception){
            log.error(e) { "Error with updated topic, creation topic unsuccessful: ${e.message}" }
            val  text = localizationService.getMessage("message.topic.create.error", locale, e.message.toString(), message.forumTopicCreated.name)

            return listOf(BotApiMethodBuilder.sendMessage(
                message.chatId.toString(),
                text = text,
                message.messageThreadId)
            )
        }
    }
}