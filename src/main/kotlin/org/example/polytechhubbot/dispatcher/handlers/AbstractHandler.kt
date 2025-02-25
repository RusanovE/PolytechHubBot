package org.example.polytechhubbot.dispatcher.handlers

import org.example.polytechhubbot.enums.UpdateType
import org.example.polytechhubbot.enums.UserType
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

interface AbstractHandler {

    fun canHandle(updateType: UpdateType): Boolean

    suspend fun answer(updateType: UpdateType, userType: UserType, update: Update): List<BotApiMethod<*>>
}