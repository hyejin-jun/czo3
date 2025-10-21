package org.mbc.czo.function.product.domain;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.mbc.czo.function.common.entity.BaseEntity;
import org.mbc.czo.function.apiUploadImage.domain.ProductImages;
import org.mbc.czo.function.product.constant.ItemSellStatus;
import org.mbc.czo.function.product.dto.ItemFormDto;
import org.mbc.czo.function.product.exception.OutOfStockException;
import org.mbc.czo.function.review.domain.Review;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="item")
@Getter
@Setter
@ToString(exclude = "images") // [수정] images 필드 순환 참조 문제 방지
public class Item extends BaseEntity {
    // BaseEntity 같은 상위 클래스에는 공통으로 들어가는 컬럼들을 정의
//예를 들어, 생성 시간(reg_time), 수정 시간(update_time), 생성자(created_by) 같은 필드가 있음
    @Id
    @Column(name="item_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;       //상품 코드

    @Column(nullable = false, length = 50)
    private String itemNm; //상품명

    @Column(name="price", nullable = false)
    private int price; //가격

    @Column(nullable = false)
    private int stockNumber; //재고수량

    @Lob
    @Column(nullable = false)
    private String itemDetail; //상품 상세 설명

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus; //상품 판매 상태

    @Column(nullable = false, columnDefinition = "int default 0")
    private int likes;      // 좋아요 수

    @Column(nullable = false, columnDefinition = "int default 0")
    private int views;      // 조회수

    /* 혜진 추가 */
    @Column(nullable = false)
    private int cost;  // 상품 원가

    // [추가] 대표 이미지 URL 필드
    private String repImgUrl;

    // 아이템 대표 이미지 매핑
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImages> images = new ArrayList<>();

    public void updateItem(ItemFormDto itemFormDto){
        this.itemNm = itemFormDto.getItemNm();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
/*        this.cost = itemFormDto.getCost();*/
    }

  /*  public void removeStock(int stockNumber){
        int restStock = this.stockNumber - stockNumber;
        if(restStock<0){
            throw new OutOfStockException("상품의 재고가 부족 합니다. (현재 재고 수량: " + this.stockNumber + ")");
        }
        this.stockNumber = restStock;
    }*/

    public void addStock(int stockNumber){
        this.stockNumber += stockNumber;
    }


    // [추가] 아이템별 리뷰 리스트
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();
}
