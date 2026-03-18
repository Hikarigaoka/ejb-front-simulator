package com.example.ejbapi.dto;

import lombok.Data;

/**
 * 注文作成リクエストDTO
 */
@Data
public class CreateOrderReq {

    /** 顧客ID */
    private String customerId;

    /** 商品ID */
    private String productId;

    /** 数量 */
    private Integer quantity;

    /** 備考 */
    private String notes;
}
