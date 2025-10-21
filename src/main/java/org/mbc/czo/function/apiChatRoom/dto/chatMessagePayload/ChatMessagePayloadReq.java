package org.mbc.czo.function.apiChatRoom.dto.chatMessagePayload;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mbc.czo.function.apiChatRoom.constant.ChatMessageStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagePayloadReq {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ChatMessageStatus messageType;
    private Long roomId;
    private String senderId;
    private String content;

    private String imageUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
