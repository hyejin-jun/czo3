package org.mbc.czo.function.chatbot.Entity;


import jakarta.persistence.*;

@Entity
@Table(name = "chatbot_reply")
public class ChatBotReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private String reply;


    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }


}