package com.example.ejbapi.controller;

import com.example.ejbapi.dto.DtoSchemaDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DTOインスペクター用コントローラー
 *
 * <p>dtoパッケージから "Req" で終わるクラスを検索し、
 * JSONスキーマ（デフォルトインスタンス）と toString() を返す。
 */
@Slf4j
@RestController
@RequestMapping("/api/dto")
@RequiredArgsConstructor
public class DtoController {

    private static final String DTO_PACKAGE = "com.example.ejbapi.dto";

    private final ObjectMapper objectMapper;

    /**
     * "Req" で終わるDTOクラス名の一覧を返す。
     *
     * <pre>GET /api/dto/list</pre>
     */
    @GetMapping("/list")
    public ResponseEntity<List<String>> listDtos() {
        List<String> result = new ArrayList<>();
        try {
            String path = DTO_PACKAGE.replace('.', '/');
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:" + path + "/**/*.class");

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null) continue;
                String simpleName = filename.replace(".class", "");
                if (simpleName.endsWith("Req")) {
                    result.add(simpleName);
                }
            }
        } catch (Exception e) {
            log.error("DTO一覧取得エラー: {}", e.getMessage(), e);
        }
        result.sort(String::compareTo);
        return ResponseEntity.ok(result);
    }

    /**
     * 指定DTOのデフォルトインスタンスJSON と toString() を返す。
     *
     * <pre>GET /api/dto/schema/{className}</pre>
     */
    @GetMapping("/schema/{className}")
    public ResponseEntity<DtoSchemaDto> getSchema(@PathVariable String className) {
        try {
            Class<?> clazz = Class.forName(DTO_PACKAGE + "." + className);
            Object instance = clazz.getDeclaredConstructor().newInstance();

            DtoSchemaDto schema = DtoSchemaDto.builder()
                    .json(objectMapper.valueToTree(instance))
                    .toStringValue(instance.toString())
                    .build();

            return ResponseEntity.ok(schema);

        } catch (Exception e) {
            log.error("DTOスキーマ取得エラー: className={} error={}", className, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
