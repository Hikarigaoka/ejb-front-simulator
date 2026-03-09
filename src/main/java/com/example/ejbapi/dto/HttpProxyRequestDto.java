package com.example.ejbapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * /api/http-proxy のリクエストDTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpProxyRequestDto {

    /** HTTPメソッド (GET / POST / PUT / DELETE / PATCH / HEAD) */
    private String method = "GET";

    /** 送信先URL */
    private String url;

    /** リクエストヘッダー */
    private Map<String, String> headers = new LinkedHashMap<>();

    /** リクエストボディ (GET/HEAD では無視) */
    private JsonNode body;
}
