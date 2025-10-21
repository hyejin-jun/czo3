package org.mbc.czo.function.apiUploadImage.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mbc.czo.function.product.domain.Item;

@Entity
@Setter
@Getter
@Table(name="productImages")
public class ProductImages extends BaseImage {

    // 아직 board 없을 때 임시 그룹핑 키
    // 게시글 생성 전은 item = null
    private String tempKey;

    @Column(length = 1)
    private String repimgYn = "N"; // 기본값 N, 대표 이미지면 Y

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    public ProductImages() {}

    public ProductImages(String originalFileName, String storedFileName, String uploadPath, String tempKey, Item item) {
        super(originalFileName, storedFileName, uploadPath);
        this.tempKey = tempKey;
        this.item = item;
    }


    public void changeItem(Item item) {
        this.item = item;
        this.tempKey = null; // 실제 게시글 연결되면 tempKey 제거
    }
}

