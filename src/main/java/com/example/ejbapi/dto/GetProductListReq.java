package com.example.ejbapi.dto;

import lombok.Data;

/**
 * 商品一覧取得リクエストDTO
 */
@Data
public class GetProductListReq {

    /** カテゴリフィルター */
    private String category;

    /** 最大取得件数 */
    private Integer maxResults;
}
