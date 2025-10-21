package org.mbc.czo.function.chatbot.Repository;

import org.mbc.czo.function.chatbot.Entity.ChatBotNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatBotNodeRepository extends JpaRepository<ChatBotNode, Long> {
    Optional<ChatBotNode> findByKeyword(String keyword);
}
