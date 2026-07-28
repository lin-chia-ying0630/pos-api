package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dto.PolicyChangeCaseDto;
import com.alin.lin.dto.PolicyChangeCaseDetailDto;
import com.alin.lin.dto.PolicyDetailDto;
import com.alin.lin.dto.PostalCodeAreaDto;
import com.alin.lin.entity.CodeDescription;
import com.alin.lin.entity.PolicyContact;
import com.alin.lin.entity.PolicyContract;
import com.alin.lin.entity.PolicyCoverage;
import com.alin.lin.entity.PolicyChangeFile;
import com.alin.lin.entity.PolicyChangeField;
import com.alin.lin.entity.PolicyChangeSnapshotField;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alin.lin.enums.PostalCodeRule;
import com.alin.lin.service.CodeDescriptionService;
import com.alin.lin.service.CurrentUserService;
import com.alin.lin.service.PolicyChangeSupportService;
import com.alin.lin.service.PolicyQueryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import static com.alin.lin.util.PolicyChangeFieldUtil.requireText;

@Service
public class PolicyQueryServiceImpl implements PolicyQueryService {
    private final PolicyChangeDao policyChangeDao;
    private final PolicyChangeSupportService policyChangeSupportService;
    private final CodeDescriptionService codeDescriptionService;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    public PolicyQueryServiceImpl(
            PolicyChangeDao policyChangeDao,
            PolicyChangeSupportService policyChangeSupportService,
            CodeDescriptionService codeDescriptionService,
            CurrentUserService currentUserService,
            ObjectMapper objectMapper
    ) {
        this.policyChangeDao = policyChangeDao;
        this.policyChangeSupportService = policyChangeSupportService;
        this.codeDescriptionService = codeDescriptionService;
        this.currentUserService = currentUserService;
        this.objectMapper = objectMapper;
    }

    @Override
    public PolicyDetailDto findPolicyDetail(String policyNo, Integer policySeq) {
        PolicyContract master = policyChangeSupportService.requirePolicy(policyNo, policySeq);
        List<PolicyContact> addressList = policyChangeDao.findAddresses(policyNo, policySeq);
        List<PolicyCoverage> rideList = policyChangeDao.findRides(policyNo, policySeq);
        // 代碼說明屬於顯示輔助資料；缺少「通訊地址」設定時仍須回傳主檔與地址清單。
        PolicyContact communicationAddress = codeDescriptionService.findCommunicationAddressCode()
                .flatMap(code -> addressList.stream()
                        .filter(address -> code.equals(address.getAddressTypeCode()))
                        .findFirst())
                .orElse(null);
        return PolicyDetailDto.builder()
                .master(master)
                .communicationAddress(communicationAddress)
                .addressList(addressList)
                .emailList(policyChangeDao.findEmails(policyNo, policySeq))
                .phoneList(policyChangeDao.findPhones(policyNo, policySeq))
                .rideList(rideList)
                .addressTypeCodes(codeDescriptionService.findAddressTypes())
                .acceptanceStatuses(codeDescriptionService.findAcceptanceStatuses())
                .changeItemCodes(codeDescriptionService.findChangeItems())
                .screenPermissions(codeDescriptionService.findScreenPermissions())
                .build();
    }

    @Override
    public PostalCodeAreaDto findPostalCodeArea(String postalCode) {
        requireText(postalCode, "postalCode");
        String normalizedPostalCode = postalCode.trim();
        if (!PostalCodeRule.ZIP_CODE_3_OR_6.matches(normalizedPostalCode)) {
            throw new IllegalArgumentException("郵遞區號前三碼必填，後三碼可空白；若填寫需為 3 碼");
        }

        String zipCode3 = normalizedPostalCode.substring(0, 3);
        CodeDescription code = codeDescriptionService.findPostalCodeZipCode3(zipCode3);
        if (code == null) {
            throw new NoSuchElementException("找不到郵遞區號前三碼: " + zipCode3);
        }

        String[] cityAndDistrict = parseCityAndDistrict(code);
        return PostalCodeAreaDto.builder()
                .postalCode(normalizedPostalCode)
                .zipCode3(zipCode3)
                .city(cityAndDistrict[0])
                .district(cityAndDistrict[1])
                .addressPrefix(String.join("", cityAndDistrict))
                .halfWidthAddressPrefix(code.getCodeDescription())
                .build();
    }

    @Override
    public List<PolicyChangeCaseDto> findChangeCases(String policyNo) {
        requireText(policyNo, "policyNo");
        List<PolicyChangeCaseDto> changeCases = policyChangeDao.findChangeCases(policyNo);
        if (!currentUserService.securityEnabled() || currentUserService.hasRole("REVIEWER")) {
            return changeCases;
        }
        String username = currentUserService.username();
        return changeCases.stream()
                .filter(changeCase -> Objects.equals(username, changeCase.getCreatedBy()))
                .toList();
    }

    @Override
    public PolicyChangeCaseDetailDto findChangeCaseDetail(String policyNo, Integer policySeq, String changeCaseNo) {
        policyChangeSupportService.requirePolicy(policyNo, policySeq);
        requireText(changeCaseNo, "changeCaseNo");
        PolicyChangeCaseDto changeCase = policyChangeDao.findChangeCase(policyNo, policySeq, changeCaseNo);
        if (changeCase == null) {
            throw new NoSuchElementException("找不到保全受理資料: " + changeCaseNo);
        }
        if (currentUserService.securityEnabled()
                && !currentUserService.hasRole("REVIEWER")
                && !Objects.equals(currentUserService.username(), changeCase.getCreatedBy())) {
            throw new NoSuchElementException("找不到保全受理資料: " + changeCaseNo);
        }
        List<PolicyChangeFile> changedRecordTypes = policyChangeDao.findChangeFilesByCaseNo(policyNo, policySeq, changeCaseNo);
        Map<String, String> chineseNames = codeDescriptionService.findChtFieldNames();
        changedRecordTypes.forEach(file -> file.setSnapshotFields(buildSnapshotFields(file, chineseNames)));
        List<PolicyChangeField> changedFieldNames = policyChangeDao.findChangeFieldsByCaseNo(
                policyNo, policySeq, changeCaseNo
        );
        changedFieldNames.forEach(field -> field.setChineseName(resolveChineseFieldName(field.getChangedFieldName(), chineseNames)));
        return PolicyChangeCaseDetailDto.builder()
                .changeCase(changeCase)
                .changedFieldNames(changedFieldNames)
                .changedRecordTypes(changedRecordTypes)
                .build();
    }

    private String resolveChineseFieldName(String changedFieldName, Map<String, String> chineseNames) {
        String simpleField = changedFieldName == null
                ? ""
                : changedFieldName.substring(changedFieldName.lastIndexOf('.') + 1);
        String jsonKey = snakeToCamel(simpleField);
        return chineseNames.getOrDefault(jsonKey, changedFieldName);
    }

    private String snakeToCamel(String value) {
        StringBuilder result = new StringBuilder();
        boolean upperNext = false;
        for (char character : value.toCharArray()) {
            if (character == '_') {
                upperNext = true;
            } else {
                result.append(upperNext ? Character.toUpperCase(character) : character);
                upperNext = false;
            }
        }
        return result.toString();
    }

    private List<PolicyChangeSnapshotField> buildSnapshotFields(
            PolicyChangeFile file,
            Map<String, String> chineseNames
    ) {
        JsonNode before = readSnapshot(file.getContentBefore());
        JsonNode after = readSnapshot(file.getContentAfter());
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        before.fieldNames().forEachRemaining(keys::add);
        after.fieldNames().forEachRemaining(keys::add);
        return keys.stream()
                .map(key -> PolicyChangeSnapshotField.builder()
                        .jsonKey(key)
                        .chineseName(chineseNames.getOrDefault(key, key))
                        .contentBefore(displayJsonValue(before.get(key)))
                        .contentAfter(displayJsonValue(after.get(key)))
                        .build())
                .toList();
    }

    private JsonNode readSnapshot(String content) {
        if (content == null || content.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(content);
            return node != null && node.isObject() ? node : objectMapper.createObjectNode();
        } catch (JsonProcessingException exception) {
            return objectMapper.createObjectNode();
        }
    }

    private String displayJsonValue(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        return value.isTextual() ? value.textValue() : value.toString();
    }

    private String[] parseCityAndDistrict(CodeDescription code) {
        String source = code.getCodeAfter() == null || code.getCodeAfter().isBlank()
                ? code.getCodeDescription()
                : code.getCodeAfter();
        String[] parts = source.split("\\|", 2);
        if (parts.length == 2) {
            return parts;
        }
        String description = code.getCodeDescription();
        if (description.length() < 4) {
            return new String[]{description, ""};
        }
        return new String[]{description.substring(0, 3), description.substring(3)};
    }
}
