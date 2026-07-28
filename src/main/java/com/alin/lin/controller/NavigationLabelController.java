package com.alin.lin.controller;

import com.alin.lin.dto.ResponseBodyDto;
import com.alin.lin.entity.CodeDescription;
import com.alin.lin.service.CodeDescriptionService;
import com.alin.lin.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 提供側邊導覽中文名稱；前端只保存穩定 key，不重複寫死中文。 */
@RestController
@RequestMapping("/api/navigation-labels")
public class NavigationLabelController {
    private final CodeDescriptionService codeDescriptionService;

    public NavigationLabelController(CodeDescriptionService codeDescriptionService) {
        this.codeDescriptionService = codeDescriptionService;
    }

    @GetMapping
    public ResponseEntity<ResponseBodyDto<List<CodeDescription>>> findNavigationLabels() {
        return ResponseUtil.ok(codeDescriptionService.findNavigationLabels());
    }
}
