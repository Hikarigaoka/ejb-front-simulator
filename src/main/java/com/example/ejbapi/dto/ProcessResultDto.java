package com.example.ejbapi.dto;

import lombok.Builder;
import lombok.Data;

/**
 * /api/process のレスポンスラッパー
 *
 * <p>リクエスト・レスポンスそれぞれのJSON表現と toString() を返す。
 */
@Data
@Builder
public class ProcessResultDto {

    /** リクエストDTOのJSON表現 */
    private Object requestJson;

    /** リクエストDTOの toString() */
    private String requestToString;

    /** レスポンスDTOのJSON表現 */
    private Object responseJson;

    /** レスポンスDTOの toString() */
    private String responseToString;
}
