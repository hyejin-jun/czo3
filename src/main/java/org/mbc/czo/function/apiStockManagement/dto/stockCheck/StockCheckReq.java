package org.mbc.czo.function.apiStockManagement.dto.stockCheck;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

@Data
@NoArgsConstructor
//@AllArgsConstructor
public class StockCheckReq {

    /**
     * 주문할 상품 ID (Item의 PK)
     * - 필수 입력 값
     */
    @NotNull(message = "상품 아이디는 필수 입력 값입니다.")
    private Long id;

    /**
     * 주문 수량
     * - 최소 1개 이상
     * - 최대 999개
     */
    @Min(value = 1, message = "최소 주문 수량은 1개입니다.")
    @Max(value = 999, message = "최대 주문 수량은 999개 입니다.")
    private int quantity;

    @NotNull
    Supplier<Integer> initialStockSupplier;

    //기존 OrderDTO때문에 임시 추가
    public StockCheckReq(Long idParam, int quantityParam,  Supplier<Integer> initialStockSupplierParam) {
        this.id = idParam;
        this.quantity = quantityParam;
        this.initialStockSupplier = initialStockSupplierParam;
    }
}
