package org.example.polytechhubbot.service

import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class LocalizationService(val messageSource: MessageSource) {

    fun getMessage(key: String, locale: Locale, vararg args: Any): String {
        return messageSource.getMessage(key, args, locale)
    }
}

