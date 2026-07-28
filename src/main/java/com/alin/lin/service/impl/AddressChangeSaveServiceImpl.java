package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dto.AddressChangeDto;
import com.alin.lin.dto.AddressChangeRequest;
import com.alin.lin.entity.PolicyContact;
import com.alin.lin.service.AddressChangeSaveService;
import com.alin.lin.service.CodeDescriptionService;
import com.alin.lin.service.PolicyChangeSupportService;
import com.alin.lin.util.PolicyChangeFieldUtil.FieldChange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.alin.lin.util.PolicyChangeFieldUtil.collectAddressFieldChanges;
import static com.alin.lin.util.PolicyChangeFieldUtil.canonicalAddress;
import static com.alin.lin.util.PolicyChangeFieldUtil.canonicalPostalCode;
import static com.alin.lin.util.PolicyChangeFieldUtil.normalizeBlank;
import static com.alin.lin.util.PolicyChangeFieldUtil.requireText;
import static com.alin.lin.util.PolicyChangeFieldUtil.validateAddressPostalCodeFormat;

@Service
public class AddressChangeSaveServiceImpl implements AddressChangeSaveService {
    private static final String ADDRESS_CHANGE_FILE = "policy_contact";

    private final PolicyChangeDao policyChangeDao;
    private final PolicyChangeSupportService policyChangeSupportService;
    private final CodeDescriptionService codeDescriptionService;
    private final ObjectMapper objectMapper;

    public AddressChangeSaveServiceImpl(
            PolicyChangeDao policyChangeDao,
            PolicyChangeSupportService policyChangeSupportService,
            CodeDescriptionService codeDescriptionService,
            ObjectMapper objectMapper
    ) {
        this.policyChangeDao = policyChangeDao;
        this.policyChangeSupportService = policyChangeSupportService;
        this.codeDescriptionService = codeDescriptionService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AddressChangeDto saveAddressChange(String changeCaseNo, AddressChangeRequest request) {
        String addressTypeCode = request.getAddressTypeCode() == null || request.getAddressTypeCode().isBlank()
                ? codeDescriptionService.communicationAddressCode()
                : request.getAddressTypeCode();

        policyChangeSupportService.requirePolicy(request.getPolicyNo(), request.getPolicySeq());
        requireText(changeCaseNo, "changeCaseNo");

        PolicyContact beforeAddress = policyChangeDao.findAddress(request.getPolicyNo(), request.getPolicySeq(), addressTypeCode);
        if (beforeAddress == null) {
            throw new NoSuchElementException("找不到地址資料: " + request.getPolicyNo() + "-" + request.getPolicySeq() + "-" + addressTypeCode);
        }

        String postalCode = normalizeBlank(request.getPostalCode());
        validateAddressRequest(addressTypeCode, postalCode, request.getAddressText());

        boolean physicalAddressType = isPhysicalAddressType(addressTypeCode);
        PolicyContact afterAddress = PolicyContact.builder()
                .addressId(beforeAddress.getAddressId())
                .policyNo(request.getPolicyNo())
                .policySeq(request.getPolicySeq())
                .addressTypeCode(addressTypeCode)
                .postalCode(postalCode)
                .addressText(normalizeBlank(request.getAddressText()))
                .countryCode(beforeAddress.getCountryCode())
                .primaryFlag(beforeAddress.getPrimaryFlag())
                .build();

        String addressChangeItem = codeDescriptionService.addressChangeItemCode();
        policyChangeSupportService.validateChangeCaseAccess(
                request.getPolicyNo(), request.getPolicySeq(), changeCaseNo, addressChangeItem
        );
        List<FieldChange> fieldChanges = collectAddressFieldChanges(beforeAddress, afterAddress);
        policyChangeDao.deleteChangeFieldsByItemAndKey(
                request.getPolicyNo(),
                request.getPolicySeq(),
                changeCaseNo,
                addressChangeItem,
                addressTypeCode
        );
        policyChangeDao.deleteChangeFileByItemAndKey(
                request.getPolicyNo(),
                request.getPolicySeq(),
                changeCaseNo,
                addressChangeItem,
                ADDRESS_CHANGE_FILE,
                addressTypeCode
        );
        if (fieldChanges.isEmpty()) {
            policyChangeSupportService.removeEmptyChangeItemAndAcceptance(
                    request.getPolicyNo(),
                    request.getPolicySeq(),
                    changeCaseNo,
                    addressChangeItem
            );
            return AddressChangeDto.builder()
                    .changeCaseNo(changeCaseNo)
                    .changeItemCode(addressChangeItem)
                    .changedFieldCount(0)
                    .build();
        }

        policyChangeSupportService.ensureChangeCaseSaved(request.getPolicyNo(), request.getPolicySeq(), changeCaseNo, addressChangeItem);
        fieldChanges.forEach(fieldChange -> policyChangeSupportService.upsertFieldChange(
                request.getPolicyNo(),
                request.getPolicySeq(),
                changeCaseNo,
                addressChangeItem,
                fieldChange
        ));

        policyChangeDao.upsertChangeFile(
                com.alin.lin.util.UuidV7.next(),
                request.getPolicyNo(),
                request.getPolicySeq(),
                changeCaseNo,
                addressChangeItem,
                ADDRESS_CHANGE_FILE,
                addressTypeCode,
                toJson(addressSnapshot(beforeAddress)),
                toJson(addressSnapshot(afterAddress))
        );

        return AddressChangeDto.builder()
                .changeCaseNo(changeCaseNo)
                .changeItemCode(addressChangeItem)
                .changedFieldCount(fieldChanges.size())
                .build();
    }

    private void validateAddressRequest(String addressTypeCode, String postalCode, String addressText) {
        if (!isPhysicalAddressType(addressTypeCode)) {
            throw new IllegalArgumentException("此 API 只接受地址資料；Email 與電話必須使用各自的異動項目");
        }
        requireText(addressText, "addressText");
        validateAddressPostalCodeFormat(postalCode);
        String postalPrefix = postalCode.substring(0, 3);
        if (codeDescriptionService.findPostalCodeZipCode3(postalPrefix) == null) {
            throw new NoSuchElementException("找不到郵遞區號前三碼: " + postalPrefix);
        }
    }

    private boolean isPhysicalAddressType(String addressTypeCode) {
        return codeDescriptionService.communicationAddressCode().equals(addressTypeCode)
                || codeDescriptionService.registeredAddressCode().equals(addressTypeCode);
    }

    private Map<String, Object> addressSnapshot(PolicyContact address) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("policyNo", address.getPolicyNo());
        snapshot.put("policySeq", address.getPolicySeq());
        snapshot.put("addressId", address.getAddressId());
        snapshot.put("addressTypeCode", address.getAddressTypeCode());
        snapshot.put("postalCode", canonicalPostalCode(address));
        snapshot.put("addressText", canonicalAddress(address));
        return snapshot;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON 轉換失敗", e);
        }
    }
}
