package org.mbc.czo.function.apiChatRoom.dto.createChatRoom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mbc.czo.function.apiChatRoom.constant.ChatRoomType;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomReq {

    private String name;

    private ChatRoomType type;

}
