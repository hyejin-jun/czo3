package org.mbc.czo.function.apiUploadImage.repository;

import org.mbc.czo.function.apiUploadImage.domain.BoardAdminImages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardAdminImageJpaRepository extends JpaRepository<BoardAdminImages, Long> {

    // tempKey 기준으로 이미지 목록 조회
    List<BoardAdminImages> findByTempKey(String tempKey);

    Optional<BoardAdminImages> findByStoredFileName(String storedFileName);
}
