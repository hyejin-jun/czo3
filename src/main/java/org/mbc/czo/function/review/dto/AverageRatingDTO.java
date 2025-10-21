package org.mbc.czo.function.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AverageRatingDTO {
    private double average; // 소수점 포함 평균
    private int rounded;    // 별 아이콘 표시용 반올림 값
}