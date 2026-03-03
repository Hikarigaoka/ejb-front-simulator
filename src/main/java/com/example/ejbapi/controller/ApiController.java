package com.example.ejbapi.controller;

import com.example.ejbapi.dto.ApiRequestDto;
import com.example.ejbapi.dto.ApiResponseDto;
import com.example.ejbapi.ejb.EjbServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST APIコントローラー
 *
 * <p>ブラウザからのHTTPリクエストを受け取り:
 * <ol>
 *   <li>リクエストJSONを {@link ApiRequestDto} にデシリアライズ</li>
 *   <li>{@link EjbServiceInterface} 経由でEJBを呼び出し</li>
 *   <li>結果 {@link ApiResponseDto} をJSONにシリアライズして返却</li>
 * </ol>
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final EjbServiceInterface ejbService;

    /**
     * EJBサービス呼び出しエンドポイント
     *
     * <pre>POST /api/process</pre>
     *
     * @param request JSONから変換されたリクエストDTO
     * @return EJBの処理結果DTO
     */
    @PostMapping("/process")
    public ResponseEntity<ApiResponseDto> process(@RequestBody ApiRequestDto request) {
        log.info(">>> POST /api/process  operationType={}", request.getOperationType());

        ApiResponseDto response = ejbService.process(request);

        log.info("<<< status={} code={}", response.getStatus(), response.getCode());
        return ResponseEntity.ok(response);
    }

    /**
     * ヘルスチェックエンドポイント
     *
     * <pre>GET /api/health</pre>
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",    "UP");
        body.put("service",   "EJB API Demo");
        body.put("timestamp", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        return ResponseEntity.ok(body);
    }
}
