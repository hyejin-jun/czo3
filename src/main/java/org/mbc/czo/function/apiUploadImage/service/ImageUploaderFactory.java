package org.mbc.czo.function.apiUploadImage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class ImageUploaderFactory {

    private final ApiMemberProfileImageUploadService memberUploader;
    private final ApiProductImageUploadService productUploader;
    private final ApiBoardAdminImageUploadService boardUploader;
    private final ApiChatRoomImageUploadService chatRoomUploader;

    public ApiImageUploader getUploader(Map<String, String> extraData) {

        if (extraData.containsKey("roomId")) {
            log.info("getUploader roomId");
            return chatRoomUploader;
        } else if (extraData.containsKey("productId")) {
            return productUploader;
        } else if (extraData.containsKey("boardId")) {
            return boardUploader;
        } else if (extraData.containsKey("userId") && extraData.get("userId") != null) {
            return memberUploader;
        }
        throw new IllegalArgumentException("ApiImageUploader: 지원하지 않는 타입");
    }

}
