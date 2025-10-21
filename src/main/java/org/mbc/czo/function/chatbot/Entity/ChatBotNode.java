package org.mbc.czo.function.chatbot.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chatbot_node")
@Getter
@Setter
public class ChatBotNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private String reply;

    @Column(columnDefinition = "json")
    private String options; // JSON 배열 문자열

    // Getter/Setter
}