package org.mbc.czo.function.apiChatRoom.dto.chatRoomParticipantDBUpdateEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mbc.czo.function.apiChatRoom.constant.ChatRoomParticipantEventType;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomParticipantDBUpdateEvent implements Serializable {

    private Long ChatRoomId;
    private String participantId;

    private ChatRoomParticipantEventType eventType;

    private int retryCount;
}
