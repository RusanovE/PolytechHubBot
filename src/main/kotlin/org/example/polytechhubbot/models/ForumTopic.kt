package org.example.polytechhubbot.models

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "forum_topics")
data class ForumTopic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "chat_id", nullable = false)
    val chatId: Long = 0,

    @Column(name = "topic_thread_id", nullable = false)
    var topicThreadId: String = "none",

    @Column(name = "topic_title", nullable = false)
    val topicTitle: String = "none title",

    @Column(name = "created_at")
    val createdAt: String? = LocalDate.now().toString(),

    @Column(name = "status")
    val status: Boolean? = false,)