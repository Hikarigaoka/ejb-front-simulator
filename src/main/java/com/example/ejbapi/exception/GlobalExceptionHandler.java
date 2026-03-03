package com.example.ejbapi.exception;

import com.example.ejbapi.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 全コントローラー共通の例外ハンドラー
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /** 不正なJSONリクエストボディ */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto> handleJsonParseError(HttpMessageNotReadableException e) {
        log.warn("JSONパースエラー: {}", e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponseDto.builder()
                        .status("ERROR")
                        .code(400)
                        .message("リクエストJSONの形式が不正です: "
                                + e.getMostSpecificCause().getMessage())
                        .timestamp(LocalDateTime.now().format(FMT))
                        .build());
    }

    /** その他の予期しない例外 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto> handleGenericError(Exception e) {
        log.error("予期しないエラー", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponseDto.builder()
                        .status("ERROR")
                        .code(500)
                        .message("サーバーエラー: " + e.getMessage())
                        .timestamp(LocalDateTime.now().format(FMT))
                        .build());
    }
}
