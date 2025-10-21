package org.mbc.czo.function.apiChatRoom.dto.createChatMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatMessageReq {

    private String content;
    private List<String> imageUrls = new ArrayList<>();
}
