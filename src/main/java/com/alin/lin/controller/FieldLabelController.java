package com.alin.lin.controller;

import com.alin.lin.dto.ResponseBodyDto;
import com.alin.lin.service.CodeDescriptionService;
import com.alin.lin.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 所有業務畫面共用的 CHT-code 欄位中文名稱，不要求使用者具有代碼維護權限。 */
@RestController
@RequestMapping("/api/field-labels")
public class FieldLabelController {
    private final CodeDescriptionService codeDescriptionService;

    public FieldLabelController(CodeDescriptionService codeDescriptionService) {
        this.codeDescriptionService = codeDescriptionService;
    }

    @GetMapping
    public ResponseEntity<ResponseBodyDto<Map<String, String>>> findFieldLabels() {
        return ResponseUtil.ok(codeDescriptionService.findChtFieldNames());
    }
}
