package org.example.polytechhubbot.repository


import org.example.polytechhubbot.models.ForumTopic
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ForumTopicRepository : CrudRepository<ForumTopic, Long> {

    fun findByChatId(chatId: Long): ForumTopic?

    fun findByTopicThreadId(topicThreadId: String): ForumTopic?

    fun findByTopicTitle(topicTitle: String): ForumTopic?

    fun existsByChatId(chatId: Long): Boolean

    fun deleteByTopicThreadId(topicThreadId: String)

}