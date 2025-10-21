package org.mbc.czo.function.apiStockManagement.controller;

import lombok.RequiredArgsConstructor;

import org.mbc.czo.function.apiStockManagement.dto.stockCheck.StockCheckReq;
import org.mbc.czo.function.apiStockManagement.service.ApiStockService;
import org.mbc.czo.function.common.apiResult.ApiResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class ApiStockController {

    ApiStockService apiStockService;

    @PatchMapping("/increase")
    public ApiResult<?> increase(@RequestBody StockCheckReq stockCheckReq) {

       try {
            apiStockService.checkAndIncreaseStock(stockCheckReq);
            return ApiResult.none();

        } catch (Exception e) {
            return ApiResult.fail(e.getMessage());
        }
    }
}
