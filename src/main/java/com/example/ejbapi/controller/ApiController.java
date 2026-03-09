package com.example.ejbapi.controller;

import com.example.ejbapi.dto.ApiRequestDto;
import com.example.ejbapi.dto.ApiResponseDto;
import com.example.ejbapi.dto.HttpProxyRequestDto;
import com.example.ejbapi.dto.ProcessResultDto;
import com.example.ejbapi.ejb.EjbServiceInterface;
import com.example.ejbapi.service.HttpProxyService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final EjbServiceInterface ejbService;
    private final ObjectMapper objectMapper;
    private final HttpProxyService httpProxyService;

    /** EJBサービス呼び出し: POST /api/process */
    @PostMapping("/process")
    public ResponseEntity<ProcessResultDto> process(@RequestBody ApiRequestDto request) {
        log.info(">>> POST /api/process  operationType={}", request.getOperationType());

        ApiResponseDto response = ejbService.process(request);

        log.info("<<< status={} code={}", response.getStatus(), response.getCode());

        ProcessResultDto result = ProcessResultDto.builder()
                .requestJson(objectMapper.valueToTree(request))
                .requestToString(request.toString())
                .responseJson(objectMapper.valueToTree(response))
                .responseToString(response.toString())
                .build();

        return ResponseEntity.ok(result);
    }

    /** 外部HTTPプロキシ: POST /api/http-proxy */
    @PostMapping("/http-proxy")
    public ResponseEntity<ProcessResultDto> httpProxy(@RequestBody HttpProxyRequestDto request) {
        return httpProxyService.proxy(request);
    }

    /** ヘルスチェック: GET /api/health */
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
