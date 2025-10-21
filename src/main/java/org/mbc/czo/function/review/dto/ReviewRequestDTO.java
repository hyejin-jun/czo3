package org.mbc.czo.function.review.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ReviewRequestDTO {
    private String memberId;   // Member PK (String)
    private Long itemId;
    private int rating;
    private String content;
    private boolean verifiedPurchase;

    private List<MultipartFile> images;
}
