package com.example.ejbapi.dto;

import lombok.Data;

/**
 * 顧客情報取得リクエストDTO
 */
@Data
public class GetCustomerReq {

    /** 顧客ID */
    private String customerId;

    /** 注文情報を含めるか */
    private Boolean includeOrders;
}
