package com.alin.lin.controller;

import com.alin.lin.dto.ResponseBodyDto;
import com.alin.lin.dto.UiFieldDefinition;
import com.alin.lin.service.PolicyUiMetadataService;
import com.alin.lin.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提供保單維護表單的資料規格；長度、精度及說明來自 main.code_definition。
 * 像素欄寬仍由前端元件管理，不由此 API 控制。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/policy-ui-metadata")
public class PolicyUiMetadataController {
    private final PolicyUiMetadataService service;

    @GetMapping("/{entity}")
    public ResponseEntity<ResponseBodyDto<List<UiFieldDefinition>>> fields(@PathVariable String entity) {
        return ResponseUtil.ok(service.fields(entity));
    }
}
