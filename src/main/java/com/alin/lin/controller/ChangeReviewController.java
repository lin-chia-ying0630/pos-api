package com.alin.lin.controller;

import com.alin.lin.dto.ResponseBodyDto;
import com.alin.lin.entity.ChangeReviewAudit;
import com.alin.lin.dto.ChangeReviewDecisionRequest;
import com.alin.lin.dto.ChangeReviewPageDto;
import com.alin.lin.service.ChangeReviewService;
import com.alin.lin.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/change-reviews")
public class ChangeReviewController {
    private final ChangeReviewService changeReviewService;

    public ChangeReviewController(ChangeReviewService changeReviewService) {
        this.changeReviewService = changeReviewService;
    }

    @GetMapping
    public ResponseEntity<ResponseBodyDto<ChangeReviewPageDto>> find(
            @RequestParam(required = false) String functionCode,
            @RequestParam(required = false) String key1,
            @RequestParam(required = false) String reviewStatus,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseUtil.ok(changeReviewService.findReviews(functionCode, key1, reviewStatus, page));
    }

    @GetMapping("/{reviewKey}/audits")
    public ResponseEntity<ResponseBodyDto<List<ChangeReviewAudit>>> findAuditTrail(
            @PathVariable String reviewKey
    ) {
        return ResponseUtil.ok(changeReviewService.findAuditTrail(reviewKey));
    }

    @PatchMapping("/{reviewKey}/decision")
    public ResponseEntity<ResponseBodyDto<String>> decide(@PathVariable String reviewKey,
                                                            @Valid @RequestBody ChangeReviewDecisionRequest request,
                                                            Authentication authentication) {
        changeReviewService.decide(
                reviewKey,
                request.getStatus(),
                request.getReviewRemark(),
                authentication.getName()
        );
        return ResponseUtil.ok("覆核處理完成");
    }
}
