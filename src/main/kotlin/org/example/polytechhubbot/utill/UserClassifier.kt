package org.example.polytechhubbot.utill

import org.example.polytechhubbot.enums.UserType
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

object UserClassifier {

    fun classify(update: Update): UserType {

        val message = update.message

        return when {
            message?.isUserMessage == true -> UserType.PERSON
            message?.isChannelMessage == true -> UserType.FROM_CHANNEL
            message?.isTopicMessage() == true || message?.isSuperGroupMessage == true || message?.isGroupMessage == true -> UserType.FROM_GROUP
            else -> UserType.UNKNOWN
        }
    }
}