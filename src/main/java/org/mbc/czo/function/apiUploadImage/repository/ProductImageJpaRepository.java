package org.mbc.czo.function.apiUploadImage.repository;

import org.mbc.czo.function.apiUploadImage.domain.ProductImages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageJpaRepository extends JpaRepository<ProductImages, Long> {

    // tempKey 기준으로 이미지 목록 조회
    List<ProductImages> findByTempKey(String tempKey);

    Optional<ProductImages> findByStoredFileName(String storedFileName);

    List<ProductImages> findByItem_Id(Long itemId);
}
