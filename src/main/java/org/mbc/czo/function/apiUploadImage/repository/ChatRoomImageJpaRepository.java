package org.mbc.czo.function.apiUploadImage.repository;

import org.mbc.czo.function.apiUploadImage.domain.ChatRoomImages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomImageJpaRepository extends JpaRepository<ChatRoomImages, Long> {

    Optional<ChatRoomImages> findByStoredFileName(String storedFileName);
}
