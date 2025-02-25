package org.example.polytechhubbot.dispatcher.handlers

import mu.KotlinLogging
import org.example.polytechhubbot.enums.UpdateType
import org.example.polytechhubbot.enums.UserType
import org.example.polytechhubbot.service.LocalizationService
import org.example.polytechhubbot.telegram.BotApiMethodBuilder
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*

@Component
class CallBackQueryHandler(val localizationService: LocalizationService): AbstractHandler {

    private val log = KotlinLogging.logger {}

    override fun canHandle(updateType: UpdateType): Boolean {
        return updateType == UpdateType.CALLBACKQUERY
    }

    override suspend fun answer(updateType: UpdateType, userType: UserType, update: Update): List<BotApiMethod<*>> {
        val userLanguage = Locale.forLanguageTag(update.callbackQuery.from.languageCode ?: "en")

        val callbackQuery = update.callbackQuery ?: return emptyList()
        val forumId = callbackQuery.message?.chatId.toString()
        val adminName = callbackQuery.from.firstName
        val callbackData = callbackQuery.data ?: return emptyList()
        log.info { "Processing callback request: $callbackData from user $forumId" }
        val (action, reason, chatId, userName) = callbackData.split("*").takeIf { it.size == 4 } ?: return emptyList()

        return when (action) {
            "reject" -> {
                handleRejectQuery(forumId, adminName, userName, reason, chatId, userLanguage)
            }
            else -> emptyList()
        }
    }

    fun handleRejectQuery(forumId: String, adminName: String, userName: String, reason: String, chatId: String, locale: Locale): List<BotApiMethod<*>>{
        val adminMessage = BotApiMethodBuilder.sendMessage(
            forumId,
            text = localizationService.getMessage("reject_query.admin", locale, adminName, userName, reason),
            )
        val userMessage = BotApiMethodBuilder.sendMessage(
            chatId,
            text = localizationService.getMessage("reject_query.user", locale, reason),
            )
        return listOf(adminMessage, userMessage)
    }

}