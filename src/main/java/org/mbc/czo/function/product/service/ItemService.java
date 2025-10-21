package org.mbc.czo.function.product.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiUploadImage.domain.ProductImages;
import org.mbc.czo.function.apiUploadImage.repository.ProductImageJpaRepository;
import org.mbc.czo.function.product.domain.Item;
import org.mbc.czo.function.product.dto.ItemFormDto;
import org.mbc.czo.function.product.dto.ItemSearchDto;
import org.mbc.czo.function.product.dto.MainItemDto;

import org.mbc.czo.function.product.repository.ItemImgRepository;
import org.mbc.czo.function.product.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ProductImageJpaRepository productImageJpaRepository;
    private final ItemImgRepository itemImgRepository;

    // 상품 등록 메서드
    public Long saveItem(ItemFormDto itemFormDto) throws Exception {
        Item item = itemFormDto.createItem();
        log.info("saveItem1: " + item.toString());
        itemRepository.save(item);
        log.info("saveItem2: " + item.toString());
        String tempKey = itemFormDto.getTempKey();
        log.info("tempKey: " + tempKey);
        if (tempKey != null) {
            List<ProductImages> tempImages = productImageJpaRepository.findByTempKey(tempKey);
            log.info("tempImagesadd: " + tempImages);
            for (int i = 0; i < tempImages.size(); i++) {
                ProductImages image = tempImages.get(i);
                image.changeItem(item);
                if (i == 0) {
                    image.setRepimgYn("Y");
                    item.setRepImgUrl("/uploads/" + image.getUploadPath());
                } else {
                    image.setRepimgYn("N");
                }
            }
            productImageJpaRepository.saveAll(tempImages);
        }
        return item.getId();
    }

    // 상품 수정 메서드
    // 상품 수정 로직을 전체적으로 새로 구현
    public void updateItem(ItemFormDto itemFormDto) {
        log.info("updateItem: " + itemFormDto);

        // 1. 상품 기본 정보 업데이트
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);
        item.setItemSellStatus(itemFormDto.getItemSellStatus());

        // 2. 이미지 삭제 및 대표 이미지 재설정
        List<String> newFileNames = itemFormDto.getFileNames();
        List<ProductImages> currentImages = productImageJpaRepository.findByItem_Id(item.getId());

        List<ProductImages> imagesToDelete = currentImages.stream()
                .filter(img -> !newFileNames.contains("/uploads/" + img.getUploadPath()))
                .collect(Collectors.toList());
        productImageJpaRepository.deleteAll(imagesToDelete);

        List<ProductImages> remainingImages = productImageJpaRepository.findByItem_Id(item.getId());
        remainingImages.forEach(img -> img.setRepimgYn("N"));

        String newRepImgUrl = itemFormDto.getRepImgUrl();
        if (newRepImgUrl != null && !newRepImgUrl.isEmpty()) {
            remainingImages.stream()
                    .filter(img -> ("/uploads/" + img.getUploadPath()).equals(newRepImgUrl))
                    .findFirst()
                    .ifPresent(repImg -> {
                        repImg.setRepimgYn("Y");
                        item.setRepImgUrl(newRepImgUrl);
                    });
        } else {
            if (!remainingImages.isEmpty()) {
                ProductImages firstImage = remainingImages.get(0);
                firstImage.setRepimgYn("Y");
                item.setRepImgUrl("/uploads/" + firstImage.getUploadPath());
            } else {
                item.setRepImgUrl(null);
            }
        }

        productImageJpaRepository.saveAll(remainingImages);

        // 3. 새로운 이미지 업로드 처리 (tempKey 기반)
        String tempKey = itemFormDto.getTempKey();
        if (tempKey != null && !tempKey.isEmpty()) {
            List<ProductImages> newImages = productImageJpaRepository.findByTempKey(tempKey);
            for (ProductImages newImage : newImages) {
                newImage.changeItem(item);
                newImage.setRepimgYn("N");
                if (newRepImgUrl != null && ("/uploads/" + newImage.getUploadPath()).equals(newRepImgUrl)) {
                    newImage.setRepimgYn("Y");
                    item.setRepImgUrl(newRepImgUrl);
                }
            }
            productImageJpaRepository.saveAll(newImages);
        }
    }

    // 등록된 상품을 불러오는 메서드 (상세페이지)
    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId) {
        itemRepository.updateView(itemId); // 조회수 증가
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);

        ItemFormDto itemFormDto = ItemFormDto.of(item);

        // Item 엔티티의 repImgUrl 필드를 직접 사용
        itemFormDto.setRepImgUrl(item.getRepImgUrl());

        // 모든 이미지 URL
        List<String> imageUrls = item.getImages()
                .stream()
                .map(img -> "/uploads/" + img.getUploadPath())
                .collect(Collectors.toList());
        itemFormDto.setFileNames(imageUrls);

        return itemFormDto;
    }

    // 좋아요를 증가시키는 서비스 메서드
    public void addLike(Long itemId) {
        itemRepository.addLike(itemId);
    }

    // 상품 조회 (관리자용)
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    // 메인페이지 상품 조회 (사용자)
    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }

    // 상품 삭제 메서드
    @Transactional
    public void deleteItem(List<Long> itemIds) {
        for (Long itemId : itemIds) {
            // 1. 상품 이미지 먼저 삭제
            itemImgRepository.deleteByItemId(itemId);
            // 2. 마지막에 상품 삭제
            itemRepository.deleteById(itemId);
        }
    }


}
