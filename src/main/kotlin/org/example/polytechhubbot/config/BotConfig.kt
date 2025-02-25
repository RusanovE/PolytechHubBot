package org.example.polytechhubbot.config

import mu.KotlinLogging
import org.example.polytechhubbot.telegram.PolytechHubBot
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession


@Configuration
class BotConfig(val polytechHubBot: PolytechHubBot) {

    private val log = KotlinLogging.logger {}

    @EventListener(ContextRefreshedEvent::class)
    @Throws(TelegramApiException::class)
    fun init() {
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        try {
            telegramBotsApi.registerBot(polytechHubBot)
        } catch (e: TelegramApiException) {
            log.error(e.message, e.printStackTrace())
        }
    }
}