package org.mbc.czo.function.apiChatRoom.dto.chatMessageDBUpdateEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mbc.czo.function.apiChatRoom.constant.ChatMessageStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDBUpdateEvent implements Serializable {

    private ChatMessageStatus status;
    private Long ChatRoomId;
    private String senderId;
    private String content;
    private LocalDateTime createdAt;

    private List<String> imageUrls;

    private int retryCount;
}
