package org.mbc.czo.function.apiUploadImage.service;

import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.apiUploadImage.domain.ProductImages;
import org.mbc.czo.function.apiUploadImage.dto.delete.ImageDeleteRef;
import org.mbc.czo.function.apiUploadImage.dto.upload.ImageUploadRef;
import org.mbc.czo.function.apiUploadImage.dto.upload.ImageUploadRes;
import org.mbc.czo.function.apiUploadImage.repository.ProductImageJpaRepository;
import org.mbc.czo.function.product.domain.Item;
import org.mbc.czo.function.product.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiProductImageUploadService implements ApiImageUploader {


    private final ProductImageJpaRepository productImageJpaRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ImageUploadRes upload(ImageUploadRef imageUploadRef) throws IOException {
        List<String> savedUrls = new ArrayList<>();
        String tempKey = null;

        for (MultipartFile file : imageUploadRef.getFiles()) {
            if (file.isEmpty()) continue;

            String productIdStr = imageUploadRef.getExtraData().get("productId");
            String relativePath = ImageUploadUtils.saveFile(file, "product");
            String originalName = file.getOriginalFilename();
            String storedName = Paths.get(relativePath).getFileName().toString();

            if ("temp".equals(productIdStr)) {
                tempKey = imageUploadRef.getExtraData().get("tempKey");
                if (tempKey == null || tempKey.isEmpty())
                    throw new IllegalArgumentException("tempKey 없음");

                ProductImages productImages = new ProductImages(originalName, storedName, relativePath, tempKey, null);
                productImageJpaRepository.save(productImages);

            } else {
                Long productId = Long.parseLong(productIdStr);
                Item product = itemRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

                ProductImages productImages = new ProductImages(originalName, storedName, relativePath, null, product);
                productImageJpaRepository.save(productImages);

                product.getImages().add(productImages);
                itemRepository.save(product);
            }

            savedUrls.add("/uploads/" + relativePath);
        }

        return ImageUploadRes.createImageUploadRes(savedUrls, tempKey);
    }

    @Override
    @Transactional
    public void deleteImage(ImageDeleteRef imageDeleteRef) throws IOException {

        // 1. product 이미지 검색
        Optional<ProductImages> productImageOpt = productImageJpaRepository.findByStoredFileName(imageDeleteRef.getStoredFileName());
        if (productImageOpt.isPresent()) {
            ProductImages image = productImageOpt.get();
            Files.deleteIfExists(Paths.get(ImageUploadUtils.UPLOAD_ROOT, image.getUploadPath()));
            productImageJpaRepository.delete(image);
            return;
        }

        // 3. 없으면 예외
        throw new IllegalArgumentException("이미지 없음");
    }
}
