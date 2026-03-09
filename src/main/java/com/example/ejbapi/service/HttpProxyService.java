package com.example.ejbapi.service;

import com.example.ejbapi.dto.HttpProxyRequestDto;
import com.example.ejbapi.dto.ProcessResultDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 外部HTTPエンドポイントへのプロキシサービス
 *
 * <p>ブラウザからのCORSを回避するため、サーバーサイドで外部HTTPリクエストを代理実行する。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HttpProxyService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private final ObjectMapper objectMapper;

    public ResponseEntity<ProcessResultDto> proxy(HttpProxyRequestDto request) {
        String method = request.getMethod() == null ? "GET" : request.getMethod().trim().toUpperCase();
        String url    = request.getUrl();

        log.info(">>> HTTP-PROXY {} {}", method, url);

        // Content-Type を解決 (ヘッダーから取得、なければ body の型で推定)
        String contentType = request.getHeaders() == null ? null
                : request.getHeaders().entrySet().stream()
                        .filter(e -> "content-type".equalsIgnoreCase(e.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst().orElse(null);

        // body 文字列化 (Content-Type に応じてエンコード方式を切り替え)
        String bodyStr = "";
        try {
            JsonNode bodyNode = request.getBody();
            if (bodyNode != null && !bodyNode.isNull()) {
                if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                    bodyStr = toFormEncoded(bodyNode);
                } else if (bodyNode.isTextual()) {
                    bodyStr = bodyNode.asText();
                } else {
                    bodyStr = objectMapper.writeValueAsString(bodyNode);
                    // Content-Type 未指定でJSONオブジェクトなら自動付与
                    if (contentType == null) {
                        request.getHeaders().putIfAbsent("Content-Type", "application/json");
                        contentType = "application/json";
                    }
                }
            }
        } catch (Exception e) {
            log.warn("body serialization failed", e);
        }

        try {
            URI uri = URI.create(url);

            HttpRequest.BodyPublisher publisher = bodyStr.isEmpty()
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(bodyStr);

            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .method(method, publisher)
                    .timeout(Duration.ofSeconds(30));

            if (request.getHeaders() != null) {
                request.getHeaders().forEach((k, v) -> {
                    if (k != null && v != null && !k.isBlank()) {
                        builder.header(k.trim(), v);
                    }
                });
            }

            HttpResponse<String> resp = HTTP_CLIENT.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());

            log.info("<<< HTTP-PROXY {} -> {}", url, resp.statusCode());

            // requestJson
            ObjectNode reqJson = objectMapper.valueToTree(request);

            // responseJson = body のみ (JSON parse できれば JsonNode, 失敗ならテキスト)
            String respBodyStr = resp.body() == null ? "" : resp.body();
            JsonNode responseBodyNode;
            try {
                responseBodyNode = respBodyStr.isEmpty()
                        ? objectMapper.nullNode()
                        : objectMapper.readTree(respBodyStr);
            } catch (Exception e) {
                responseBodyNode = objectMapper.getNodeFactory().textNode(respBodyStr);
            }

            // requestToString
            String reqToString = buildRequestToString(method, url, request.getHeaders(), bodyStr);

            // responseToString
            String resToString = buildResponseToString(resp.statusCode(), resp.headers().map(), respBodyStr);

            ProcessResultDto result = ProcessResultDto.builder()
                    .requestJson(reqJson)
                    .requestToString(reqToString)
                    .responseJson(responseBodyNode)
                    .responseToString(resToString)
                    .build();

            return ResponseEntity.status(resp.statusCode()).body(result);

        } catch (Exception e) {
            log.error("HTTP-PROXY error: {}", e.getMessage(), e);

            ObjectNode reqJson  = objectMapper.valueToTree(request);
            ObjectNode errJson  = objectMapper.createObjectNode();
            errJson.put("error",   "Proxy Error");
            errJson.put("message", e.getMessage());

            ProcessResultDto result = ProcessResultDto.builder()
                    .requestJson(reqJson)
                    .requestToString(method + " " + url)
                    .responseJson(errJson)
                    .responseToString("Error: " + e.getMessage())
                    .build();

            return ResponseEntity.status(502).body(result);
        }
    }

    /** JSON オブジェクトを application/x-www-form-urlencoded 形式に変換 */
    private String toFormEncoded(JsonNode node) {
        if (!node.isObject()) {
            return URLEncoder.encode(node.asText(), StandardCharsets.UTF_8);
        }
        List<String> pairs = new ArrayList<>();
        node.fields().forEachRemaining(e -> {
            String key = URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8);
            String val = URLEncoder.encode(
                    e.getValue().isTextual() ? e.getValue().asText() : e.getValue().toString(),
                    StandardCharsets.UTF_8);
            pairs.add(key + "=" + val);
        });
        return String.join("&", pairs);
    }

    private String buildRequestToString(String method, String url,
                                        Map<String, String> headers, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(' ').append(url).append('\n');
        if (headers != null) {
            headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append('\n'));
        }
        if (!body.isEmpty()) {
            sb.append('\n').append(body);
        }
        return sb.toString();
    }

    private String buildResponseToString(int status, Map<String, List<String>> headers, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status).append('\n');
        if (headers != null) {
            headers.forEach((k, v) -> {
                if (!k.startsWith(":")) {   // HTTP/2 pseudo-headers を除外
                    sb.append(k).append(": ").append(String.join(", ", v)).append('\n');
                }
            });
        }
        if (!body.isEmpty()) {
            sb.append('\n').append(body);
        }
        return sb.toString();
    }
}
