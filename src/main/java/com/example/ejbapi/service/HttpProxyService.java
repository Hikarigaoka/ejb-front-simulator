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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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

        // body文字列化
        String bodyStr = "";
        try {
            JsonNode bodyNode = request.getBody();
            if (bodyNode != null && !bodyNode.isNull()) {
                bodyStr = bodyNode.isTextual()
                        ? bodyNode.asText()
                        : objectMapper.writeValueAsString(bodyNode);
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
