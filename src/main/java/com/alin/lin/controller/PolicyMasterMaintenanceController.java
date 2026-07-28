package com.alin.lin.controller;

import com.alin.lin.dto.PolicyMasterMaintenanceRequest;
import com.alin.lin.dto.ResponseBodyDto;
import com.alin.lin.entity.PolicyContract;
import com.alin.lin.service.PolicyMasterMaintenanceService;
import com.alin.lin.service.PolicyUiMetadataService;
import com.alin.lin.util.ResponseUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policy-masters")
public class PolicyMasterMaintenanceController {
    private final PolicyMasterMaintenanceService service;
    private final PolicyUiMetadataService metadataService;

    public PolicyMasterMaintenanceController(PolicyMasterMaintenanceService service, PolicyUiMetadataService metadataService) {
        this.service = service;
        this.metadataService = metadataService;
    }

    @PostMapping
    public ResponseEntity<ResponseBodyDto<PolicyContract>> create(@Valid @RequestBody PolicyMasterMaintenanceRequest request,
                                                                    Authentication authentication) {
        metadataService.validate("master", request.asFieldMap());
        return ResponseUtil.created(service.create(request, authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<ResponseBodyDto<PolicyContract>> update(@Valid @RequestBody PolicyMasterMaintenanceRequest request,
                                                                    Authentication authentication) {
        metadataService.validate("master", request.asFieldMap());
        return ResponseUtil.ok(service.update(request, authentication.getName()));
    }

    @DeleteMapping("/{policyNo}/{policySeq}")
    public ResponseEntity<ResponseBodyDto<Void>> delete(@PathVariable String policyNo, @PathVariable Integer policySeq,
                                                        Authentication authentication) {
        service.delete(policyNo, policySeq, authentication.getName());
        return ResponseUtil.noContent("保單主檔已刪除");
    }
}
