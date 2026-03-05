package com.example.ejbapi.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTOスキーマ情報レスポンス
 *
 * <p>指定DTOのデフォルトインスタンスをJSON化したものと toString() を返す。
 */
@Data
@Builder
public class DtoSchemaDto {

    /** DTOのJSON表現（フィールド確認用テンプレート） */
    private Object json;

    /** DTOの toString() 表現 */
    private String toStringValue;
}
