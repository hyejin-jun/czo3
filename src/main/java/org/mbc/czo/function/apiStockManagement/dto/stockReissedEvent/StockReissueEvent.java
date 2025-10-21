package org.mbc.czo.function.apiStockManagement.dto.stockReissedEvent;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReissueEvent implements Serializable {

    @NotNull(message = "상품 아이디는 필수 입력 값입니다.")
    private Long id;

    @NotNull()
    private int remainStock;

    @NotNull()
    private int ReissuedThreshold;

    private int retryCount;
}
