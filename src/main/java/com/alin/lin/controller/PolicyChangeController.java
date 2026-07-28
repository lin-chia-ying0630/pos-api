package com.alin.lin.controller;

import com.alin.lin.dto.AddressChangeDto;
import com.alin.lin.dto.AddressChangeRequest;
import com.alin.lin.dto.CreateChangeCaseDto;
import com.alin.lin.dto.CreateChangeCaseRequest;
import com.alin.lin.dto.ContactChannelChangeRequest;
import com.alin.lin.dto.ChangeCaseEligibilityDto;
import com.alin.lin.dto.MainAmountChangeDto;
import com.alin.lin.dto.MainAmountChangeRequest;
import com.alin.lin.dto.PolicyChangeCaseDto;
import com.alin.lin.dto.PolicyChangeCaseDetailDto;
import com.alin.lin.dto.PolicyDetailDto;
import com.alin.lin.dto.PostalCodeAreaDto;
import com.alin.lin.dto.ResponseBodyDto;
import com.alin.lin.dto.RiderAmountChangeListRequest;
import com.alin.lin.dto.UpdateChangeCaseStatusDto;
import com.alin.lin.dto.UpdateChangeCaseStatusRequest;
import com.alin.lin.service.PolicyChangeService;
import com.alin.lin.util.ResponseUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api")
public class PolicyChangeController {
    private final PolicyChangeService policyChangeService;

    public PolicyChangeController(PolicyChangeService policyChangeService) {
        this.policyChangeService = policyChangeService;
    }

    // 畫面對應：申請保全變更頁載入保單主檔、通訊地址、全部地址清單、主附約資料與變更項目。
    @GetMapping("/policies/{policyNo}/{policySeq}")
    public ResponseEntity<ResponseBodyDto<PolicyDetailDto>> findPolicyDetail(
            @PathVariable @NotBlank(message = "policyNo 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POLICY_NO, message = "policyNo 格式錯誤") String policyNo,
            @PathVariable @NotNull(message = "policySeq 不可空白") @Positive(message = "policySeq 必須大於 0") Integer policySeq
    ) {
        return ResponseUtil.ok(policyChangeService.findPolicyDetail(policyNo, policySeq));
    }

    // 畫面對應：申請保全變更頁的地址變更 Dialog，輸入 3+3 郵遞區號後帶入地址前綴。
    @GetMapping("/postal-codes/{postalCode}")
    public ResponseEntity<ResponseBodyDto<PostalCodeAreaDto>> findPostalCodeArea(
            @PathVariable
            @NotBlank(message = "postalCode 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POSTAL_CODE, message = "postalCode 必須為 3 或 6 碼數字")
            String postalCode
    ) {
        return ResponseUtil.ok(policyChangeService.findPostalCodeArea(postalCode));
    }

    // 畫面對應：申請保全變更頁產生案號前，依保單與保全變更項目檢查最近案件是否仍為 P-受理中。
    @GetMapping("/policies/{policyNo}/{policySeq}/change-items/{changeItemCode}/eligibility")
    public ResponseEntity<ResponseBodyDto<ChangeCaseEligibilityDto>> checkChangeCaseEligibility(
            @PathVariable @NotBlank(message = "policyNo 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POLICY_NO, message = "policyNo 格式錯誤") String policyNo,
            @PathVariable @NotNull(message = "policySeq 不可空白") @Positive(message = "policySeq 必須大於 0") Integer policySeq,
            @PathVariable @NotBlank(message = "changeItemCode 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.CHANGE_ITEM, message = "changeItemCode 格式錯誤") String changeItemCode
    ) {
        return ResponseUtil.ok(policyChangeService.checkChangeCaseEligibility(policyNo, policySeq, changeItemCode));
    }

    // 畫面對應：申請保全變更頁的「產生案號」按鈕，只先取得 P-受理中案號，不立即寫受理檔。
    @PostMapping("/change-cases")
    public ResponseEntity<ResponseBodyDto<CreateChangeCaseDto>> createChangeCase(@Valid @RequestBody CreateChangeCaseRequest request) {
        return ResponseUtil.created(policyChangeService.createChangeCase(request));
    }

    // 畫面對應：申請保全變更頁的 001 地址變更 Dialog 儲存。
    @PostMapping("/change-cases/{changeCaseNo}/address-change")
    public ResponseEntity<ResponseBodyDto<AddressChangeDto>> saveAddressChange(
            @PathVariable @NotBlank(message = "changeCaseNo 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.CHANGE_CASE_NO, message = "changeCaseNo 格式錯誤") String changeCaseNo,
            @Valid @RequestBody AddressChangeRequest request
    ) {
        return ResponseUtil.ok(policyChangeService.saveAddressChange(changeCaseNo, request));
    }

    // 畫面對應：004～006 電子郵件、市內電話、行動電話變更 Dialog 儲存。
    @PostMapping("/change-cases/{changeCaseNo}/contact-channels/{channel}")
    public ResponseEntity<ResponseBodyDto<AddressChangeDto>> saveContactChannelChange(
            @PathVariable @NotBlank(message = "changeCaseNo 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.CHANGE_CASE_NO, message = "changeCaseNo 格式錯誤")
            String changeCaseNo,
            @PathVariable @Pattern(regexp = "email|telephone|mobile", message = "channel 格式錯誤") String channel,
            @Valid @RequestBody ContactChannelChangeRequest request
    ) {
        return ResponseUtil.ok(policyChangeService.saveContactChannelChange(changeCaseNo, channel, request));
    }

    // 畫面對應：申請保全變更頁的 002 主約保額變更 Dialog 儲存。
    @PostMapping("/change-cases/{changeCaseNo}/main-amount-change")
    public ResponseEntity<ResponseBodyDto<MainAmountChangeDto>> saveMainAmountChange(
            @PathVariable @NotBlank(message = "changeCaseNo 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.CHANGE_CASE_NO, message = "changeCaseNo 格式錯誤") String changeCaseNo,
            @Valid @RequestBody MainAmountChangeRequest request
    ) {
        return ResponseUtil.ok(policyChangeService.saveMainAmountChange(changeCaseNo, request));
    }

    // 畫面對應：申請保全變更頁的 003 附約保額變更 Dialog 儲存。
    @PostMapping("/change-cases/{changeCaseNo}/policies/{policyNo}/{policySeq}/rider-amount-change")
    public ResponseEntity<ResponseBodyDto<MainAmountChangeDto>> saveRiderAmountChange(
            @PathVariable @NotBlank(message = "changeCaseNo 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.CHANGE_CASE_NO, message = "changeCaseNo 格式錯誤") String changeCaseNo,
            @PathVariable @NotBlank(message = "policyNo 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POLICY_NO, message = "policyNo 格式錯誤") String policyNo,
            @PathVariable @NotNull(message = "policySeq 不可空白") @Positive(message = "policySeq 必須大於 0") Integer policySeq,
            @Valid @RequestBody RiderAmountChangeListRequest request
    ) {
        return ResponseUtil.ok(policyChangeService.saveRiderAmountChange(changeCaseNo, policyNo, policySeq, request));
    }

    // 畫面對應：查詢保全變更頁與覆核頁，依保單號碼列出既有保全受理資料。
    @GetMapping("/policies/{policyNo}/change-cases")
    public ResponseEntity<ResponseBodyDto<List<PolicyChangeCaseDto>>> findChangeCases(
            @PathVariable @NotBlank(message = "policyNo 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POLICY_NO, message = "policyNo 格式錯誤") String policyNo
    ) {
        return ResponseUtil.ok(policyChangeService.findChangeCases(policyNo));
    }

    // 畫面對應：查詢保全變更頁與覆核頁展開案件，顯示每個欄位及檔案快照的異動前後值。
    @GetMapping("/policies/{policyNo}/{policySeq}/change-cases/{changeCaseNo}")
    public ResponseEntity<ResponseBodyDto<PolicyChangeCaseDetailDto>> findChangeCaseDetail(
            @PathVariable @NotBlank(message = "policyNo 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POLICY_NO, message = "policyNo 格式錯誤") String policyNo,
            @PathVariable @NotNull(message = "policySeq 不可空白") @Positive(message = "policySeq 必須大於 0") Integer policySeq,
            @PathVariable @NotBlank(message = "changeCaseNo 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.CHANGE_CASE_NO, message = "changeCaseNo 格式錯誤") String changeCaseNo
    ) {
        return ResponseUtil.ok(policyChangeService.findChangeCaseDetail(policyNo, policySeq, changeCaseNo));
    }

    // 畫面對應：覆核頁將 P-受理中案件改為 S-完成或 C-取消，完成時才回寫主檔、地址或主附約。
    @PatchMapping("/change-cases/{changeCaseNo}/status")
    public ResponseEntity<ResponseBodyDto<UpdateChangeCaseStatusDto>> updateChangeCaseStatus(
            @PathVariable @NotBlank(message = "changeCaseNo 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.CHANGE_CASE_NO, message = "changeCaseNo 格式錯誤") String changeCaseNo,
            @Valid @RequestBody UpdateChangeCaseStatusRequest request
    ) {
        return ResponseUtil.ok(policyChangeService.updateChangeCaseStatus(changeCaseNo, request));
    }
}
