package org.example.polytechhubbot.dispatcher.handlers

import mu.KotlinLogging
import org.example.polytechhubbot.enums.Emoji
import org.example.polytechhubbot.enums.UpdateType
import org.example.polytechhubbot.enums.UserType
import org.example.polytechhubbot.service.ForumTopicService
import org.example.polytechhubbot.service.LocalizationService
import org.example.polytechhubbot.telegram.BotApiMethodBuilder
import org.example.polytechhubbot.telegram.PolytechHubBot
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*

@Component
class CommandHandler(val forumTopicService: ForumTopicService, val localizationService: LocalizationService,@Lazy val polytechHubBot: PolytechHubBot) : AbstractHandler {

    private val log = KotlinLogging.logger {}

    @Value("\${adminForum.id}")
    var forumId: Long = 0

    @Value("\${secretGroup.id}")
    var secretGroupChatId: Long = 0

    override fun canHandle(updateType: UpdateType): Boolean {
        return updateType.toString().endsWith("COMMAND")
    }

    override suspend fun answer(updateType: UpdateType, userType: UserType, update: Update): List<BotApiMethod<*>> {

        val userLanguage = Locale.forLanguageTag(update.message.from.languageCode ?: "en")

        return when(updateType) {
            UpdateType.START_COMMAND -> {
                if (userType == UserType.PERSON) handleStart(update.message, userLanguage)
                else {
                    val text = localizationService.getMessage("start.not_person", userLanguage)
                    listOf(BotApiMethodBuilder.sendMessage(update.message.chatId.toString(), text, update.message.messageThreadId))
                }
            }
            UpdateType.CHECK_CHANNEL_STATUS_COMMAND -> {
                if (userType == UserType.PERSON) handleCheckStatus(update.message, userLanguage)
                else {
                    val text = localizationService.getMessage("check_status.not_person", userLanguage)
                    listOf(BotApiMethodBuilder.sendMessage(update.message.chatId.toString(), text, update.message.messageThreadId))
                }
            }
            UpdateType.HOW_TO_COMMUNICATE_COMMAND -> {
                if (userType == UserType.PERSON || userType == UserType.FROM_GROUP) handleHowToCommunicate(update.message, userType, userLanguage)
                else {
                    val text = localizationService.getMessage("how_communicate.channel", userLanguage)
                    listOf(BotApiMethodBuilder.sendMessage(update.message.chatId.toString(), text, update.message.messageThreadId))
                }
            }
            UpdateType.HELP_COMMAND -> {
               if (userType == UserType.PERSON || userType == UserType.FROM_GROUP) handleHelp(update.message, userType, userLanguage)
               else {
                   val text = localizationService.getMessage("help.channel", userLanguage)
                   listOf(BotApiMethodBuilder.sendMessage(update.message.chatId.toString(), text, update.message.messageThreadId))
               }
            }
            UpdateType.APPROVE_COMMAND ->{
                if (userType == UserType.FROM_GROUP && update.message.chatId == forumId)handleApprove(update.message, userLanguage)
                else{
                    val text = localizationService.getMessage("approve.not_admin", userLanguage)
                    listOf(BotApiMethodBuilder.sendMessage(update.message.chatId.toString(), text, update.message.messageThreadId))
                }
            }
            UpdateType.DISAPPROVE_COMMAND ->{
                if (userType == UserType.FROM_GROUP && update.message.chatId == forumId)handleDisapprove(update.message, userLanguage)
                else{
                    val text = localizationService.getMessage("disapprove.not_admin", userLanguage)
                    listOf(BotApiMethodBuilder.sendMessage(update.message.chatId.toString(), text, update.message.messageThreadId))
                }
            }
            UpdateType.UNKNOWN -> {
            log.warn { "Unknown update type: $updateType. From: $userType, username: ${update.message.from.firstName}" }
            emptyList()
        }
            else -> emptyList()
        }
    }

    private suspend fun handleApprove(message: Message, locale: Locale): List<BotApiMethod<*>> {

        try {
            val topic = forumTopicService.findTopicByThreadId(message.messageThreadId?.toString() ?: "")
                ?: throw IllegalStateException("Topic not found where /approve was received")

            val inviteLinkModel = BotApiMethodBuilder.createChatInviteLink(
                secretGroupChatId.toString(),
                name = "LinkFor${topic.topicTitle}"
            )
            val inviteLink = polytechHubBot.executeSafely(inviteLinkModel)?.join()?.inviteLink

            val approveMessage = BotApiMethodBuilder.sendMessage(
                topic.chatId.toString(),
                text = localizationService.getMessage("approve.success", locale, inviteLink.toString())
            )

            val adminConfirmApproveMessage = BotApiMethodBuilder.sendMessage(
                forumId.toString(),
                text = localizationService.getMessage("approve.admin_notification", locale, topic.topicTitle),//.replace("{0}", topic.topicTitle),
                message.messageThreadId
            )
            log.warn("User ${topic.topicTitle} has been given a link to enter a private chat.")
            return listOf(approveMessage,adminConfirmApproveMessage)

        } catch (e: IllegalStateException) {
            log.error("Error while processing command /approve: ${e.message}")
            return listOf(BotApiMethodBuilder.sendMessage(
                message.chatId.toString(),
                text = e.message ?: "An error occurred.",
                message.messageThreadId)
            )
        } catch (e: Exception) {
            log.error("An unexpected error occurred while processing the /approve command. ${e.message}")
            return listOf(BotApiMethodBuilder.sendMessage(
                message.chatId.toString(),
                text = e.message ?: "An unexpected error has occurred. Please contact the developer.",
                message.messageThreadId)
            )
        }
    }

    private fun handleDisapprove(message: Message, locale: Locale): List<BotApiMethod<*>> {
        try {
            val topic = forumTopicService.findTopicByThreadId(message.messageThreadId?.toString() ?: "")
                ?: throw IllegalStateException("The topic where the /disapprove command was received was not found.")

            val rejectionReasons = listOf(
                "Doubtful-account",
                "Violation-of-rules",
                "Scam",
                "Spam",
                "Another"
            )

            val keyboard = BotApiMethodBuilder.createInlineKeyboard(
                rejectionReasons.associateWith { reason -> "reject*${reason}*${topic.chatId}*${topic.topicTitle}" }
            )

            val disapproveMessage = BotApiMethodBuilder.sendMessage(
                forumId.toString(),
                text = localizationService.getMessage("disapprove.choose_reason", locale),
                message.messageThreadId,
                replyMarkup = keyboard
            )

            log.info("Requested to reject the request in the topic: ${topic.topicTitle}")
            return listOf(disapproveMessage)
        } catch (e: Exception) {
            log.error("Error processing command /disapprove: ${e.message}")
            return listOf(BotApiMethodBuilder.sendMessage(
                message.chatId.toString(),
                text = e.message ?: "An error occurred while processing the command.",
                message.messageThreadId
            ))
        }
    }

    private fun handleHowToCommunicate(message: Message, userType: UserType, locale: Locale): List<BotApiMethod<*>> {

        val chatId = message.chatId
        val helpMessage = if (userType == UserType.PERSON) {
            localizationService.getMessage("how_communicate.person", locale)
        } else {
            localizationService.getMessage("how_communicate.group", locale)
        }

        return listOf(BotApiMethodBuilder.sendMessage(
            chatId.toString(),
            text = helpMessage,
            message.messageThreadId)
        )
    }

    private fun handleCheckStatus(message: Message, locale: Locale): List<BotApiMethod<*>> {
        log.info("User ${message.from.firstName} (${message.chatId}) decide check connection with forum")

        try {
            val existTopic = forumTopicService.findTopicByChatId(message.chatId) ?: throw IllegalStateException("There is no specified topic for user ${message.from.firstName}")
            val topicStatus = existTopic.status

            return if (existTopic.topicThreadId != "none" && topicStatus == true)
                listOf(BotApiMethodBuilder.sendMessage(
                    message.chatId.toString(),
                    text = localizationService.getMessage("check_status.ok", locale,topicStatus.toString(), Emoji.PILL.value))
                )
            else if (existTopic.topicThreadId == "none" || !topicStatus!!)
                listOf(BotApiMethodBuilder.sendMessage(
                    message.chatId.toString(),
                    text = localizationService.getMessage("check_status.bad", locale, topicStatus.toString(), Emoji.SCULL.value))
                )
            else  listOf(BotApiMethodBuilder.sendMessage(
                message.chatId.toString(),
                text = localizationService.getMessage("check_status.not_created", locale, Emoji.ALIEN.value))
            )
        }catch (e: Exception){
            log.warn("We searched non-existence topic for user ${message.from.firstName}")
            return listOf(BotApiMethodBuilder.sendMessage(
                message.chatId.toString(),
                text = localizationService.getMessage("check_status.error", locale, Emoji.IDOL.value))
            )
        }
    }

    private fun handleHelp(message: Message, userType: UserType, locale: Locale): List<BotApiMethod<*>> {

        val chatId = message.chatId
        val helpMessage = if (userType == UserType.PERSON) {
            localizationService.getMessage("help.person", locale)
        } else {
            localizationService.getMessage("help.group", locale)
        }

        return listOf(BotApiMethodBuilder.sendMessage(
            chatId.toString(),
            text = helpMessage,
            message.messageThreadId)
        )
    }

    private fun handleStart(message: Message, locale: Locale): List<BotApiMethod<*>> {
        val username = message.from.userName ?: message.from.firstName
        val chatId = message.chatId
        val topicThreadId = message.messageThreadId.toString()
        val topicName = "$username ($chatId)"

        log.info("Creating a new topic: $topicName in the forum $forumId")

        val text = localizationService.getMessage("start.start", locale, Emoji.ANGEL.value)

        if (forumTopicService.isExistForumTopic(chatId))
            return listOf(BotApiMethodBuilder.sendMessage(
                chatId.toString(),
                text = text))
        else {
            forumTopicService.createForumTopic(chatId, topicThreadId, topicName)
            return listOf(BotApiMethodBuilder.createForumTopic(
                forumId.toString(),
                topicName,
                Emoji.entries.random().value)
            )
        }
    }
}