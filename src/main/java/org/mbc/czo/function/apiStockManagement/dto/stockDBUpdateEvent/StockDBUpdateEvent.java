package org.mbc.czo.function.apiStockManagement.dto.stockDBUpdateEvent;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDBUpdateEvent implements Serializable {

    @NotNull(message = "상품 아이디는 필수 입력 값입니다.")
    private Long id;

    @NotNull()
    private int remainStock;

    private int retryCount;
}
