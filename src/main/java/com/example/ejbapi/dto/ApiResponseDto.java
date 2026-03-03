package com.example.ejbapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * EJB呼び出し結果レスポンスDTO
 *
 * <p>EJBサービスの戻り値を本クラスにシリアライズし、
 * JSONとしてブラウザへ返却する。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto {

    /** 処理ステータス: "SUCCESS" | "ERROR" */
    private String status;

    /** HTTPステータスに対応するコード */
    private int code;

    /** 処理結果メッセージ */
    private String message;

    /** EJBから返却されたビジネスデータ */
    private Object data;

    /** リクエストの操作種別 (エコーバック用) */
    private String operationType;

    /** 処理完了タイムスタンプ (ISO-8601) */
    private String timestamp;
}
