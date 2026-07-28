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

@RestController
@RequestMapping("/api/function-codes")
public class FunctionCodeController {
    private final CodeDescriptionService codeDescriptionService;

    public FunctionCodeController(CodeDescriptionService codeDescriptionService) {
        this.codeDescriptionService = codeDescriptionService;
    }

    // 畫面對應：所有作業畫面右上方的功能代碼標籤。
    @GetMapping
    public ResponseEntity<ResponseBodyDto<List<CodeDescription>>> findFunctionCodes() {
        return ResponseUtil.ok(codeDescriptionService.findScreenFunctionCodes());
    }
}
