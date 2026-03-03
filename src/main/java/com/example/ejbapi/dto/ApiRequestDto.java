package com.example.ejbapi.dto;

import lombok.Data;

import java.util.Map;

/**
 * EJB呼び出し用リクエストDTO
 *
 * <p>ブラウザから受け取ったJSONを本クラスにデシリアライズし、
 * EJBサービスインターフェースへ渡す。
 */
@Data
public class ApiRequestDto {

    /** 実行するEJB操作の種別 (例: "getCustomer", "createOrder") */
    private String operationType;

    /** 操作固有のパラメータマップ */
    private Map<String, Object> data;
}
