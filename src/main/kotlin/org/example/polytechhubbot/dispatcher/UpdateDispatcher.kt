package org.example.polytechhubbot.dispatcher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.example.polytechhubbot.dispatcher.handlers.CallBackQueryHandler
import org.example.polytechhubbot.dispatcher.handlers.CommandHandler
import org.example.polytechhubbot.dispatcher.handlers.MessageHandler
import org.example.polytechhubbot.utill.UpdateClassifier
import org.example.polytechhubbot.utill.UserClassifier
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
class UpdateDispatcher(commandHandler: CommandHandler, messageHandler: MessageHandler, callBackQueryHandler: CallBackQueryHandler) {
    private val log = KotlinLogging.logger {}

    private val handlers = listOf(commandHandler, messageHandler, callBackQueryHandler)

    suspend fun dispatch(update: Update): List<BotApiMethod<*>> = withContext(Dispatchers.Default) {
        try {
            val updateType = UpdateClassifier.classify(update)
            val userType = UserClassifier.classify(update)

            handlers.firstOrNull { it.canHandle(updateType) }
                ?.answer(updateType, userType, update) ?: emptyList()
        } catch (e: TelegramApiException) {
            log.info("Some trouble with dispatch method: ${e.message}")
            emptyList()
        }
    }
}

