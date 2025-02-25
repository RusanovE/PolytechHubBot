package org.example.polytechhubbot.telegram

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.example.polytechhubbot.config.BotProperties
import org.example.polytechhubbot.dispatcher.UpdateDispatcher
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.Serializable
import java.util.concurrent.CompletableFuture

@Service
class PolytechHubBot(val botProperties: BotProperties, val updateDispatcher: UpdateDispatcher) : TelegramLongPollingBot(botProperties.token) {

    private val log = KotlinLogging.logger {}

    override fun getBotUsername(): String = botProperties.username

    override fun onUpdateReceived(update: Update?) {
        if (update != null && (update.hasMessage() || update.hasCallbackQuery())) {
            CoroutineScope(Dispatchers.IO).launch {
                processUpdate(update)
            }
        } else {
            log.warn("Unsupported update: ${update?.message.toString()}")
        }
    }

    private suspend fun processUpdate(update: Update) = coroutineScope {
        val methods = updateDispatcher.dispatch(update)
        methods.forEach { method ->
            launch {
                try {
                    executeSafely(method)
                } catch (e: Exception) {
                    log.warn("Error executing method: ${e.message}")
                    val text = "Something went wrong. Please try again later."
                    executeSafely(BotApiMethodBuilder.sendMessage(update.message.chatId.toString(), text, update.message.messageThreadId))
                }
            }
        }
    }

    suspend fun <T : Serializable, Method : BotApiMethod<T>> executeSafely(method: Method): CompletableFuture<T>? {
        return withContext(Dispatchers.IO) {
            try {
                executeAsync(method)
            } catch (e: TelegramApiException) {
                log.error(e) { "Method execution error: ${method.method}" }
                null
            }
        }
    }
}


