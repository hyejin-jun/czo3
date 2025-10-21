package org.mbc.czo.function.apiUploadImage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiChatRoom.RedisChatMessagePublisher;
import org.mbc.czo.function.apiChatRoom.constant.ChatMessageStatus;
import org.mbc.czo.function.apiChatRoom.dto.chatImageDBUpdateEvent.ChatImageDBUpdateEvent;
import org.mbc.czo.function.apiChatRoom.dto.chatMessageDBUpdateEvent.ChatMessageDBUpdateEvent;
import org.mbc.czo.function.apiChatRoom.kafka.producer.ChatImageDBUpdateProducer;
import org.mbc.czo.function.apiChatRoom.repository.ChatMessageJpaRepository;
import org.mbc.czo.function.apiChatRoom.repository.ChatRoomJpaRepository;
import org.mbc.czo.function.apiUploadImage.domain.ChatRoomImages;
import org.mbc.czo.function.apiUploadImage.dto.delete.ImageDeleteRef;
import org.mbc.czo.function.apiUploadImage.dto.upload.ImageUploadRef;
import org.mbc.czo.function.apiUploadImage.dto.upload.ImageUploadRes;
import org.mbc.czo.function.apiUploadImage.repository.ChatRoomImageJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApiChatRoomImageUploadService implements ApiImageUploader {

    private final ChatRoomImageJpaRepository chatRoomImageJpaRepository;
    private final ChatRoomJpaRepository chatRoomJpaRepository;

    private final ChatImageDBUpdateProducer chatImageDBUpdateProducer;
    @Override
    @Transactional
    public ImageUploadRes upload(ImageUploadRef imageUploadRef) throws IOException {
        List<String> savedUrls = new ArrayList<>();

        for (MultipartFile file : imageUploadRef.getFiles()) {
            if (file.isEmpty()) continue;

            String roomIdStr = imageUploadRef.getExtraData().get("roomId");
            String userId = imageUploadRef.getExtraData().get("userId");
            Long chatRoomId = Long.parseLong(roomIdStr);

            boolean existsChatRoom = chatRoomJpaRepository.existsById(chatRoomId);
            if (!existsChatRoom) {
                throw new IllegalArgumentException("채팅방 없음");
            }

            String relativePath = ImageUploadUtils.saveFile(file, "chatroom");
            String originalName = file.getOriginalFilename();
            String storedName = Paths.get(relativePath).getFileName().toString();


            chatImageDBUpdateProducer.sendChatImageDBUpdateEvent(
                    new ChatImageDBUpdateEvent(
                            ChatMessageStatus.IMAGE, chatRoomId, userId, "",
                            relativePath, originalName, storedName, 0));

            savedUrls.add("/uploads/" + relativePath);
        }

        return ImageUploadRes.createImageUploadRes(savedUrls, null);
    }

    @Override
    @Transactional
    public void deleteImage(ImageDeleteRef imageDeleteRef) throws IOException {
        ChatRoomImages image = chatRoomImageJpaRepository.findByStoredFileName(imageDeleteRef.getStoredFileName())
                .orElseThrow(() -> new IllegalArgumentException("이미지 없음"));

        Files.deleteIfExists(Paths.get(ImageUploadUtils.UPLOAD_ROOT, image.getUploadPath()));
        chatRoomImageJpaRepository.delete(image);
    }
}
