package org.mbc.czo.function.apiChatRoom.dto.chatImageDBUpdateEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mbc.czo.function.apiChatRoom.constant.ChatMessageStatus;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatImageDBUpdateEvent implements Serializable {

    private ChatMessageStatus status;
    private Long ChatRoomId;
    private String senderId;
    private String content;
    private String originalName;
    private String storedName;
    private String relativePath;

    private int retryCount;
}
