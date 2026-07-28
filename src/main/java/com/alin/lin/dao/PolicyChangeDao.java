package com.alin.lin.dao;

import com.alin.lin.dto.PolicyChangeCaseDto;
import com.alin.lin.entity.CodeDescription;
import com.alin.lin.entity.ChangeReview;
import com.alin.lin.entity.ChangeReviewAudit;
import com.alin.lin.entity.PolicyContact;
import com.alin.lin.entity.PolicyContract;
import com.alin.lin.entity.PolicyCoverage;
import com.alin.lin.entity.PolicyEmail;
import com.alin.lin.entity.PolicyPhone;
import com.alin.lin.entity.PolicyChangeAcceptance;
import com.alin.lin.entity.PolicyChangeCaseReservation;
import com.alin.lin.entity.PolicyChangeCaseReservationItem;
import com.alin.lin.entity.PolicyChangeField;
import com.alin.lin.entity.PolicyChangeFile;
import com.alin.lin.entity.PolicyChangeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * MyBatis 直接產生此 DAO 的代理實作，維持 Controller -> Service -> DAO 三層架構。
 */
@Mapper
public interface PolicyChangeDao {
    PolicyContract findMaster(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq);

    PolicyContract findMasterForUpdate(@Param("policyNo") String policyNo,
                                         @Param("policySeq") Integer policySeq);

    int insertPolicyMaster(PolicyContract master);
    int updatePolicyMaster(com.alin.lin.dto.PolicyMasterMaintenanceRequest request);
    int deletePolicyMaster(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq,
                           @Param("updatedBy") String updatedBy);
    int updatePolicyMasterReviewDecision(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq,
                                         @Param("status") String status, @Param("reviewedBy") String reviewedBy);
    int updateCodeReviewDecision(@Param("codeGroup") String codeGroup, @Param("codeField") String codeField,
                                 @Param("codeBefore") String codeBefore, @Param("status") String status,
                                 @Param("reviewedBy") String reviewedBy);
    int insertPolicyAddress(PolicyContact address);
    int updatePolicyAddress(PolicyContact address);
    int deletePolicyAddress(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq,
                            @Param("addressTypeCode") String addressTypeCode, @Param("updatedBy") String updatedBy);
    int updatePolicyAddressReviewDecision(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq,
                                          @Param("addressTypeCode") String addressTypeCode, @Param("status") String status,
                                          @Param("reviewedBy") String reviewedBy);
    int insertPolicyRide(PolicyCoverage ride);
    int updatePolicyRide(PolicyCoverage ride);
    int deletePolicyRide(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq,
                         @Param("coverageItemSeq") String coverageItemSeq, @Param("updatedBy") String updatedBy);
    int updatePolicyRideReviewDecision(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq,
                                       @Param("coverageItemSeq") String coverageItemSeq, @Param("status") String status,
                                       @Param("reviewedBy") String reviewedBy);

    PolicyContact findAddress(@Param("policyNo") String policyNo,
                                  @Param("policySeq") Integer policySeq,
                                  @Param("addressTypeCode") String addressTypeCode);

    PolicyContact findAddressForUpdate(@Param("policyNo") String policyNo,
                                           @Param("policySeq") Integer policySeq,
                                           @Param("addressTypeCode") String addressTypeCode);

    List<PolicyContact> findAddresses(@Param("policyNo") String policyNo,
                                          @Param("policySeq") Integer policySeq);
    List<PolicyEmail> findEmails(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq);
    List<PolicyPhone> findPhones(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq);
    PolicyEmail findEmail(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq,
                          @Param("contactId") String contactId);
    PolicyPhone findPhone(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq,
                          @Param("contactId") String contactId);
    int updateEmailValue(@Param("contactId") String contactId, @Param("beforeValue") String beforeValue,
                         @Param("afterValue") String afterValue);
    int updatePhoneValue(@Param("contactId") String contactId, @Param("beforeValue") String beforeValue,
                         @Param("afterValue") String afterValue);
    int insertEmailValue(@Param("contactId") String contactId, @Param("policyNo") String policyNo,
                         @Param("policySeq") Integer policySeq, @Param("afterValue") String afterValue);
    int insertPhoneValue(@Param("contactId") String contactId, @Param("policyNo") String policyNo,
                         @Param("policySeq") Integer policySeq, @Param("phoneTypeCode") String phoneTypeCode,
                         @Param("afterValue") String afterValue);

    List<PolicyCoverage> findRides(@Param("policyNo") String policyNo,
                                   @Param("policySeq") Integer policySeq);
    PolicyCoverage findRide(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq,
                            @Param("coverageItemSeq") String coverageItemSeq);
    PolicyCoverage findRideForUpdate(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq,
                                     @Param("coverageItemSeq") String coverageItemSeq);

    List<PolicyCoverage> findRidesForUpdate(@Param("policyNo") String policyNo,
                                            @Param("policySeq") Integer policySeq);

    List<CodeDescription> findCodes(@Param("codeGroup") String codeGroup, @Param("codeField") String codeField);

    List<CodeDescription> findCodesByGroup(@Param("codeGroup") String codeGroup);
    List<CodeDescription> findUiFieldDefinitions(@Param("codeGroup") String codeGroup);
    List<CodeDescription> findAllCodes();
    int insertCode(CodeDescription code);
    int updateCode(com.alin.lin.dto.CodeDescriptionCreateRequest code);
    int deleteCode(@Param("codeGroup") String codeGroup, @Param("codeField") String codeField,
                   @Param("codeBefore") String codeBefore, @Param("updatedBy") String updatedBy);
    int reviewCode(@Param("codeGroup") String codeGroup, @Param("codeField") String codeField,
                   @Param("codeBefore") String codeBefore, @Param("reviewedBy") String reviewedBy);

    int insertChangeReview(ChangeReview review);
    int insertCompletedChangeReview(ChangeReview review);
    ChangeReview findChangeReviewForUpdate(@Param("reviewKey") String reviewKey);
    ChangeReview findPendingChangeReviewForUpdate(@Param("functionCode") String functionCode,
                                                  @Param("uniqueKey") String uniqueKey);
    int insertChangeReviewAudit(ChangeReviewAudit audit);
    List<ChangeReviewAudit> findChangeReviewAudits(@Param("reviewKey") String reviewKey);
    List<ChangeReview> findChangeReviews(@Param("functionCode") String functionCode,
                                         @Param("key1") String key1,
                                         @Param("reviewStatus") String reviewStatus,
                                         @Param("limit") int limit,
                                         @Param("offset") int offset);
    long countChangeReviews(@Param("functionCode") String functionCode,
                            @Param("key1") String key1,
                            @Param("reviewStatus") String reviewStatus);
    int updateChangeReviewStatus(@Param("reviewKey") String reviewKey,
                                 @Param("status") String status,
                                 @Param("reviewRemark") String reviewRemark,
                                 @Param("reviewedBy") String reviewedBy);

    int acquirePendingReviewLock(@Param("functionCode") String functionCode,
                                 @Param("uniqueKey") String uniqueKey,
                                 @Param("reviewKey") String reviewKey,
                                 @Param("createdBy") String createdBy);
    int releasePendingReviewLock(@Param("reviewKey") String reviewKey);

    int applyPolicyContractUpdate(@Param("value") PolicyContract value,
                                  @Param("expectedVersion") Long expectedVersion,
                                  @Param("operatorId") String operatorId);
    int applyPolicyContractDelete(@Param("policyNo") String policyNo, @Param("policySeq") Integer policySeq,
                                  @Param("expectedVersion") Long expectedVersion,
                                  @Param("operatorId") String operatorId);
    int applyPolicyContactUpdate(@Param("value") PolicyContact value,
                                 @Param("expectedVersion") Long expectedVersion,
                                 @Param("operatorId") String operatorId);
    int applyPolicyContactDelete(@Param("value") PolicyContact value,
                                 @Param("expectedVersion") Long expectedVersion,
                                 @Param("operatorId") String operatorId);
    int applyPolicyCoverageUpdate(@Param("value") PolicyCoverage value,
                                  @Param("expectedVersion") Long expectedVersion,
                                  @Param("operatorId") String operatorId);
    int applyPolicyCoverageDelete(@Param("value") PolicyCoverage value,
                                  @Param("expectedVersion") Long expectedVersion,
                                  @Param("operatorId") String operatorId);
    int applyCodeDefinitionUpdate(@Param("before") CodeDescription before,
                                  @Param("value") CodeDescription value,
                                  @Param("expectedVersion") Long expectedVersion,
                                  @Param("operatorId") String operatorId);
    int applyCodeDefinitionDelete(@Param("value") CodeDescription value,
                                  @Param("expectedVersion") Long expectedVersion,
                                  @Param("operatorId") String operatorId);

    CodeDescription findCode(@Param("codeGroup") String codeGroup,
                             @Param("codeField") String codeField,
                             @Param("codeBefore") String codeBefore);

    int incrementCaseSequence(@Param("sequenceDate") LocalDate sequenceDate);

    Long findLastInsertedSequence();

    int insertCaseReservation(PolicyChangeCaseReservation reservation);

    int insertCaseReservationItem(PolicyChangeCaseReservationItem reservationItem);

    PolicyChangeCaseReservation findCaseReservationForUpdate(@Param("changeCaseNo") String changeCaseNo);

    List<String> findReservedChangeItems(@Param("changeCaseNo") String changeCaseNo);

    int consumeCaseReservation(@Param("changeCaseNo") String changeCaseNo,
                               @Param("reservedBy") String reservedBy);

    List<PolicyChangeCaseDto> findChangeCases(@Param("policyNo") String policyNo);

    PolicyChangeCaseDto findChangeCase(@Param("policyNo") String policyNo,
                                       @Param("policySeq") Integer policySeq,
                                       @Param("changeCaseNo") String changeCaseNo);

    // 依保單與保全變更項目取得最近一筆已受理案件，供申請資格檢核。
    PolicyChangeCaseDto findLatestChangeCaseByItem(@Param("policyNo") String policyNo,
                                                    @Param("policySeq") Integer policySeq,
                                                    @Param("changeItemCode") String changeItemCode);

    PolicyChangeAcceptance findAcceptanceForUpdate(@Param("policyNo") String policyNo,
                                                    @Param("policySeq") Integer policySeq,
                                                    @Param("changeCaseNo") String changeCaseNo);

    List<String> findChangeItemsByCaseNo(@Param("policyNo") String policyNo,
                                         @Param("policySeq") Integer policySeq,
                                         @Param("changeCaseNo") String changeCaseNo);

    List<PolicyChangeFile> findChangeFilesByItem(@Param("policyNo") String policyNo,
                                                 @Param("policySeq") Integer policySeq,
                                                 @Param("changeCaseNo") String changeCaseNo,
                                                 @Param("changeItemCode") String changeItemCode);

    List<PolicyChangeField> findChangeFieldsByItem(@Param("policyNo") String policyNo,
                                                   @Param("policySeq") Integer policySeq,
                                                   @Param("changeCaseNo") String changeCaseNo,
                                                   @Param("changeItemCode") String changeItemCode);

    List<PolicyChangeFile> findChangeFilesByCaseNo(@Param("policyNo") String policyNo,
                                                   @Param("policySeq") Integer policySeq,
                                                   @Param("changeCaseNo") String changeCaseNo);

    List<PolicyChangeField> findChangeFieldsByCaseNo(@Param("policyNo") String policyNo,
                                                     @Param("policySeq") Integer policySeq,
                                                     @Param("changeCaseNo") String changeCaseNo);

    int insertAcceptance(PolicyChangeAcceptance acceptance);

    int insertChangeItem(PolicyChangeItem changeItemCode);

    int existsChangeItem(@Param("policyNo") String policyNo,
                         @Param("policySeq") Integer policySeq,
                         @Param("changeCaseNo") String changeCaseNo,
                         @Param("changeItemCode") String changeItemCode);

    int upsertChangeField(@Param("changeFieldId") String changeFieldId,
                          @Param("policyNo") String policyNo,
                          @Param("policySeq") Integer policySeq,
                          @Param("changeCaseNo") String changeCaseNo,
                          @Param("changeItemCode") String changeItemCode,
                          @Param("changedFieldName") String changedFieldName,
                          @Param("changedRecordKey") String changedRecordKey,
                          @Param("contentBefore") String contentBefore,
                          @Param("contentAfter") String contentAfter);

    int upsertChangeFile(@Param("changeSnapshotId") String changeSnapshotId,
                         @Param("policyNo") String policyNo,
                         @Param("policySeq") Integer policySeq,
                         @Param("changeCaseNo") String changeCaseNo,
                         @Param("changeItemCode") String changeItemCode,
                         @Param("changedRecordType") String changedRecordType,
                         @Param("changedRecordKey") String changedRecordKey,
                         @Param("contentBefore") String contentBefore,
                         @Param("contentAfter") String contentAfter);

    int deleteChangeFieldsByItem(@Param("policyNo") String policyNo,
                                 @Param("policySeq") Integer policySeq,
                                 @Param("changeCaseNo") String changeCaseNo,
                                 @Param("changeItemCode") String changeItemCode);

    int deleteChangeFieldsByItemAndKey(@Param("policyNo") String policyNo,
                                       @Param("policySeq") Integer policySeq,
                                       @Param("changeCaseNo") String changeCaseNo,
                                       @Param("changeItemCode") String changeItemCode,
                                       @Param("changedRecordKey") String changedRecordKey);

    int deleteChangeFileByItemAndKey(@Param("policyNo") String policyNo,
                                     @Param("policySeq") Integer policySeq,
                                     @Param("changeCaseNo") String changeCaseNo,
                                     @Param("changeItemCode") String changeItemCode,
                                     @Param("changedRecordType") String changedRecordType,
                                     @Param("changedRecordKey") String changedRecordKey);

    int countChangeFieldsByItem(@Param("policyNo") String policyNo,
                                @Param("policySeq") Integer policySeq,
                                @Param("changeCaseNo") String changeCaseNo,
                                @Param("changeItemCode") String changeItemCode);

    int countChangeFilesByItem(@Param("policyNo") String policyNo,
                               @Param("policySeq") Integer policySeq,
                               @Param("changeCaseNo") String changeCaseNo,
                               @Param("changeItemCode") String changeItemCode);

    int deleteChangeItem(@Param("policyNo") String policyNo,
                         @Param("policySeq") Integer policySeq,
                         @Param("changeCaseNo") String changeCaseNo,
                         @Param("changeItemCode") String changeItemCode);

    int countChangeItemsByCaseNo(@Param("policyNo") String policyNo,
                                 @Param("policySeq") Integer policySeq,
                                 @Param("changeCaseNo") String changeCaseNo);

    int deleteAcceptance(@Param("policyNo") String policyNo,
                         @Param("policySeq") Integer policySeq,
                         @Param("changeCaseNo") String changeCaseNo,
                         @Param("expectedStatus") String expectedStatus);

    int updateAcceptanceStatusIfCurrent(@Param("acceptance") PolicyChangeAcceptance acceptance,
                                        @Param("expectedStatus") String expectedStatus);

    int updateAddress(PolicyContact address);

    int updateRideAmount(@Param("policyNo") String policyNo,
                         @Param("policySeq") Integer policySeq,
                         @Param("coverageItemSeq") String coverageItemSeq,
                         @Param("insuredAmount") String insuredAmount);

    int updateRidePremium(@Param("policyNo") String policyNo,
                          @Param("policySeq") Integer policySeq,
                          @Param("coverageItemSeq") String coverageItemSeq,
                          @Param("premiumAmount") String premiumAmount);

    int updateMasterTotalPremiumFromRides(@Param("policyNo") String policyNo,
                                          @Param("policySeq") Integer policySeq);
}
