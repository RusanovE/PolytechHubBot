package org.example.polytechhubbot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class BotProperties (

    @Value("\${bot.username}")
    val username: String,

    @Value("\${bot.token}")
    val token: String
)