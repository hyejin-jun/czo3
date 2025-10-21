package org.mbc.czo.function.review.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mbc.czo.function.product.domain.Item;
import org.mbc.czo.function.apiMember.domain.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    // ⭐ Member의 PK는 mid이고, DB 컬럼명은 member_id이므로
    // referencedColumnName을 명시적으로 지정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private int rating;

    @Column(length = 1000, nullable = false)
    private String content;

    private boolean verifiedPurchase;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImg> images = new ArrayList<>();

    public void addImage(ReviewImg image) {
        images.add(image);
        image.setReview(this);
    }
}