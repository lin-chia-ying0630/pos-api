package com.alin.lin.controller;

import com.alin.lin.dto.ResponseBodyDto;
import com.alin.lin.dto.PolicyContactMaintenanceRequest;
import com.alin.lin.dto.PolicyCoverageMaintenanceRequest;
import com.alin.lin.entity.PolicyContact;
import com.alin.lin.entity.PolicyCoverage;
import com.alin.lin.service.PolicyDetailMaintenanceService;
import com.alin.lin.service.PolicyUiMetadataService;
import com.alin.lin.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/policy-details")
public class PolicyDetailMaintenanceController {
    private final PolicyDetailMaintenanceService service;
    private final PolicyUiMetadataService metadataService;
    public PolicyDetailMaintenanceController(PolicyDetailMaintenanceService service, PolicyUiMetadataService metadataService) {
        this.service = service;
        this.metadataService = metadataService;
    }

    @PostMapping("/addresses")
    public ResponseEntity<ResponseBodyDto<PolicyContact>> createAddress(@Valid @RequestBody PolicyContactMaintenanceRequest request, Authentication auth) {
        metadataService.validate("address", request.asFieldMap());
        return ResponseUtil.created(service.createAddress(request.toEntity(), auth.getName()));
    }
    @PutMapping("/addresses")
    public ResponseEntity<ResponseBodyDto<PolicyContact>> updateAddress(@Valid @RequestBody PolicyContactMaintenanceRequest request, Authentication auth) {
        metadataService.validate("address", request.asFieldMap());
        return ResponseUtil.ok(service.updateAddress(request.toEntity(), auth.getName()));
    }
    @DeleteMapping("/addresses/{policyNo}/{policySeq}/{addressTypeCode}")
    public ResponseEntity<ResponseBodyDto<Void>> deleteAddress(@PathVariable String policyNo, @PathVariable Integer policySeq,
                                                               @PathVariable String addressTypeCode, Authentication auth) {
        service.deleteAddress(policyNo, policySeq, addressTypeCode, auth.getName()); return ResponseUtil.noContent("保單地址已刪除");
    }
    @PostMapping({"/rides", "/coverages"})
    public ResponseEntity<ResponseBodyDto<PolicyCoverage>> createRide(@Valid @RequestBody PolicyCoverageMaintenanceRequest request, Authentication auth) {
        metadataService.validate("ride", request.asFieldMap());
        return ResponseUtil.created(service.createRide(request.toEntity(), auth.getName()));
    }
    @PutMapping({"/rides", "/coverages"})
    public ResponseEntity<ResponseBodyDto<PolicyCoverage>> updateRide(@Valid @RequestBody PolicyCoverageMaintenanceRequest request, Authentication auth) {
        metadataService.validate("ride", request.asFieldMap());
        return ResponseUtil.ok(service.updateRide(request.toEntity(), auth.getName()));
    }
    @DeleteMapping({"/rides/{policyNo}/{policySeq}/{coverageItemSeq}", "/coverages/{policyNo}/{policySeq}/{coverageItemSeq}"})
    public ResponseEntity<ResponseBodyDto<Void>> deleteRide(@PathVariable String policyNo, @PathVariable Integer policySeq,
                                                            @PathVariable String coverageItemSeq, Authentication auth) {
        service.deleteRide(policyNo, policySeq, coverageItemSeq, auth.getName()); return ResponseUtil.noContent("保單主附約已刪除");
    }
}
