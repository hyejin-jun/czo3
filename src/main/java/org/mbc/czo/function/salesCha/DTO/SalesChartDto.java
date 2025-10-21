package org.mbc.czo.function.salesCha.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesChartDto {
    private String period;       // "2025-09" or "2025-09-09"
    private long totalRevenue;   // 총 매출액
    private long totalCost;      // 총 매출원가
    private long profit;         // 당기순이익
    private long deliveryFee;    // 판관비 (배송비)

    public SalesChartDto(String period, long totalRevenue, long totalCost, long profit, long deliveryFee) {
        this.period = period;
        this.totalRevenue = totalRevenue;
        this.totalCost = totalCost;
        this.profit = profit;
        this.deliveryFee = deliveryFee;
    }
}