package com.alin.lin.service;

import com.alin.lin.entity.CodeDescription;
import com.alin.lin.dto.CodeDescriptionCreateRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CodeDescriptionService {
    List<CodeDescription> findAllCodes();
    CodeDescription createCode(CodeDescriptionCreateRequest request, String username);
    CodeDescription updateCode(CodeDescriptionCreateRequest request, String username);
    void deleteCode(String codeGroup, String codeField, String codeBefore, String username);
    CodeDescription reviewCode(String codeGroup, String codeField, String codeBefore, String reviewedBy);
    List<CodeDescription> findAddressTypes();

    List<CodeDescription> findAcceptanceStatuses();

    List<CodeDescription> findChangeItems();

    List<CodeDescription> findScreenPermissions();

    List<CodeDescription> findScreenFunctionCodes();

    List<CodeDescription> findNavigationLabels();

    List<CodeDescription> findUserAuthorizationPermissions();

    CodeDescription findPostalCodeZipCode3(String zipCode3);

    Map<String, String> findChtFieldNames();

    String communicationAddressCode();

    /** 查詢畫面使用的容錯版本；代碼未設定時不得讓整份保單資料查詢失敗。 */
    Optional<String> findCommunicationAddressCode();

    String registeredAddressCode();

    String emailAddressCode();

    String addressChangeItemCode();

    String mainAmountChangeItemCode();

    String riderAmountChangeItemCode();

    String emailChangeItemCode();

    String telephoneChangeItemCode();

    String mobileChangeItemCode();

    String pendingStatusCode();

    String processingStatusCode();

    String completeStatusCode();

    String cancelStatusCode();

    String mainRideTypeCode();
}
