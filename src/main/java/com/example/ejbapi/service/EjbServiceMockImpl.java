package com.example.ejbapi.service;

import com.example.ejbapi.dto.ApiRequestDto;
import com.example.ejbapi.dto.ApiResponseDto;
import com.example.ejbapi.ejb.EjbServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * EJBサービス モック実装
 *
 * <p>開発・デモ用のインメモリ実装。
 * 本番では JNDI 経由で取得したリモートスタブに差し替える。
 */
@Slf4j
@Service
public class EjbServiceMockImpl implements EjbServiceInterface {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public ApiResponseDto process(ApiRequestDto request) {
        log.info("EJB呼び出し開始: operationType={}", request.getOperationType());

        try {
            Object result = dispatch(request);

            log.info("EJB呼び出し完了: operationType={}", request.getOperationType());
            return ApiResponseDto.builder()
                    .status("SUCCESS")
                    .code(200)
                    .message("処理が正常に完了しました")
                    .operationType(request.getOperationType())
                    .data(result)
                    .timestamp(LocalDateTime.now().format(FMT))
                    .build();

        } catch (Exception e) {
            log.error("EJB呼び出しエラー: {}", e.getMessage(), e);
            return ApiResponseDto.builder()
                    .status("ERROR")
                    .code(500)
                    .message("EJB処理中にエラーが発生しました: " + e.getMessage())
                    .operationType(request.getOperationType())
                    .timestamp(LocalDateTime.now().format(FMT))
                    .build();
        }
    }

    /** operationType に応じてモックレスポンスを返す */
    private Object dispatch(ApiRequestDto req) {
        return switch (String.valueOf(req.getOperationType())) {

            case "getCustomer" -> {
                Map<String, Object> customer = new LinkedHashMap<>();
                customer.put("customerId",  param(req, "customerId", "C000"));
                customer.put("name",        "山田 太郎");
                customer.put("email",       "yamada@example.com");
                customer.put("phone",       "03-1234-5678");
                customer.put("status",      "ACTIVE");
                customer.put("grade",       "GOLD");

                if (Boolean.TRUE.equals(param(req, "includeOrders", false))) {
                    customer.put("orders", List.of(
                        Map.of("orderId", "ORD-2026-001", "amount", 15000,
                               "status", "SHIPPED",   "date", "2026-02-20"),
                        Map.of("orderId", "ORD-2026-002", "amount", 8500,
                               "status", "PROCESSING","date", "2026-03-01")
                    ));
                }
                yield customer;
            }

            case "createOrder" -> {
                Map<String, Object> order = new LinkedHashMap<>();
                order.put("orderId",           "ORD-" + System.currentTimeMillis());
                order.put("status",            "CREATED");
                order.put("estimatedDelivery", "2026-03-10");
                order.put("requestedItems",    req.getData());
                yield order;
            }

            case "getProductList" -> {
                yield List.of(
                    Map.of("productId","P001","name","ノートPC","price",120000,"stock",15),
                    Map.of("productId","P002","name","マウス",   "price",  3500,"stock",80),
                    Map.of("productId","P003","name","キーボード","price",  8000,"stock",40)
                );
            }

            default -> {
                Map<String, Object> echo = new LinkedHashMap<>();
                echo.put("echo",          req.getData());
                echo.put("note",          "operationType '" + req.getOperationType() + "' のモックレスポンス");
                echo.put("processingNode","MockEJB-01");
                yield echo;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T param(ApiRequestDto req, String key, T defaultValue) {
        if (req.getData() == null) return defaultValue;
        Object v = req.getData().get(key);
        return v != null ? (T) v : defaultValue;
    }
}
