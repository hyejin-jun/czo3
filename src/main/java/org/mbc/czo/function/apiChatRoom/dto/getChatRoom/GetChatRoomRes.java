package org.mbc.czo.function.apiChatRoom.dto.getChatRoom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetChatRoomRes {

    private Long id;
    private String name;
    private String type; // 예: GROUP, PRIVATE
    private int participantNumber;
    //비밀번호 등

    public static GetChatRoomRes createGetAllChatRoomRes(Long idParam, String nameParam, String typeParam, int participantNumberParam) {
        return  GetChatRoomRes.builder()
                .id(idParam)
                .name(nameParam)
                .type(typeParam)
                .participantNumber(participantNumberParam)
                .build();
    }
}
