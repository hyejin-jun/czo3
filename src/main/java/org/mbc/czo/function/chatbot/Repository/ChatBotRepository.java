package org.mbc.czo.function.chatbot.Repository;

import org.mbc.czo.function.chatbot.Entity.ChatBotReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatBotRepository extends JpaRepository<ChatBotReply, Long> {
    List<ChatBotReply> findByKeywordContaining(String keyword);
}