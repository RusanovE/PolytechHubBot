package org.example.polytechhubbot.utill

import org.example.polytechhubbot.enums.UpdateType
import org.telegram.telegrambots.meta.api.objects.Update

object UpdateClassifier {

    fun classify(update: Update): UpdateType {
        val message = update.message
        val callbackQuery = update.callbackQuery

        return when {
            message?.isCommand == true && message.text?.startsWith("/") == true -> {
                when (message.text) {
                    "/start", "/start@op_hub_bot" -> UpdateType.START_COMMAND
                    "/help", "/help@op_hub_bot" -> UpdateType.HELP_COMMAND
                    "/check_status", "/check_status@op_hub_bot" -> UpdateType.CHECK_CHANNEL_STATUS_COMMAND
                    "/how_to_communicate", "/how_to_communicate@op_hub_bot" ->  UpdateType.HOW_TO_COMMUNICATE_COMMAND
                    "/approve", "/approve@op_hub_bot" -> UpdateType.APPROVE_COMMAND
                    "/disapprove", "/disapprove@op_hub_bot" -> UpdateType.DISAPPROVE_COMMAND
                    else -> UpdateType.UNKNOWN
                }
            }
            message?.text?.contains("#ToForum") == true -> UpdateType.TO_FORUM
            message?.text?.contains("#FromForum") == true -> UpdateType.FROM_FORUM
            message?.forumTopicCreated != null -> UpdateType.CREATED_NEW_TOPIC
            message?.forumTopicClosed != null -> UpdateType.CLOSED_TOPIC
            callbackQuery != null -> UpdateType.CALLBACKQUERY
            else -> UpdateType.UNKNOWN
        }
    }
}

