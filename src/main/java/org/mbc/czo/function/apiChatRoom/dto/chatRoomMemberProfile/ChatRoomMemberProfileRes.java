package org.mbc.czo.function.apiChatRoom.dto.chatRoomMemberProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomMemberProfileRes {

    private String id;
    private String name;
    private String profileImagePath;

    public static ChatRoomMemberProfileRes createChatRoomMemberProfileRes(String idParam, String nameParam, String profileImagePathParam) {
        return  ChatRoomMemberProfileRes.builder()
                .id(idParam)
                .name(nameParam)
                .profileImagePath(profileImagePathParam)
                .build();
    }
}
