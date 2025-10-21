package org.mbc.czo.function.apiUploadImage.domain;

import jakarta.persistence.*;
import org.mbc.czo.function.apiChatRoom.domain.ChatMessage;

@Entity
@Table(name="chatRoomImages")
public class ChatRoomImages extends BaseImage {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id")
    private ChatMessage chatMessage;

    public ChatRoomImages() {}

    public ChatRoomImages(String originalFileName, String storedFileName, String uploadPath, ChatMessage chatMessage) {
        super(originalFileName, storedFileName, uploadPath);
        this.chatMessage = chatMessage;
    }

}

