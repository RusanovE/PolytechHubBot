package org.example.polytechhubbot.service

import jakarta.transaction.Transactional
import org.example.polytechhubbot.models.ForumTopic
import org.example.polytechhubbot.repository.ForumTopicRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ForumTopicService(val forumTopicRepository: ForumTopicRepository) {

    /**
     * Создает новый топик и сохраняет его в базе данных.
     */
    fun createForumTopic(chatId: Long, topicThreadId: String, topicTitle: String): ForumTopic {
        val forumTopic = ForumTopic(
            chatId = chatId,
            topicThreadId = topicThreadId,
            topicTitle = topicTitle,
            createdAt = LocalDate.now().toString(),
            status = true
        )

        return forumTopicRepository.save(forumTopic)
    }

    /**
     * Ищет топик по chatId.
     */
    fun findTopicByChatId(chatId: Long): ForumTopic? {
        return forumTopicRepository.findByChatId(chatId)
    }

    /**
     * Ищет топик по названию.
     */
    fun findTopicByTitle(topicTitle: String): ForumTopic? {
        return forumTopicRepository.findByTopicTitle(topicTitle)
    }

    fun findTopicByThreadId(topicThreadId: String): ForumTopic? {
        return forumTopicRepository.findByTopicThreadId(topicThreadId)
    }

    /**
     * Получает все топики.
     */
    fun getAllTopics(): List<ForumTopic> { //TODO(create command for this )
        return forumTopicRepository.findAll().toList()
    }

    fun isExistForumTopic(chatId: Long): Boolean{
        return forumTopicRepository.existsByChatId(chatId)
    }

    @Transactional
    fun updateForumTopic(topicTitle: String, messageThreadId: Int): ForumTopic? {
        val existForumTopic = findTopicByTitle(topicTitle)
            ?: throw  IllegalStateException("There is no such topic $topicTitle")

        existForumTopic.topicThreadId = messageThreadId.toString()
        forumTopicRepository.save(existForumTopic)
        return  existForumTopic
    }

    @Transactional
    fun deleteTopicByThreadId(messageThreadId: String) {
        forumTopicRepository.deleteByTopicThreadId(messageThreadId)
    }
}