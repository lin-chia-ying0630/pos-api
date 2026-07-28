package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dto.AddressChangeDto;
import com.alin.lin.dto.ContactChannelChangeRequest;
import com.alin.lin.entity.PolicyEmail;
import com.alin.lin.entity.PolicyPhone;
import com.alin.lin.service.CodeDescriptionService;
import com.alin.lin.service.ContactChannelChangeService;
import com.alin.lin.service.PolicyChangeSupportService;
import com.alin.lin.util.PolicyChangeFieldUtil.FieldChange;
import com.alin.lin.util.UuidV7;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Objects;

import static com.alin.lin.util.PolicyChangeFieldUtil.normalizeBlank;
import static com.alin.lin.util.PolicyChangeFieldUtil.requireText;

@Service
public class ContactChannelChangeServiceImpl implements ContactChannelChangeService {
    private final PolicyChangeDao dao;
    private final PolicyChangeSupportService support;
    private final CodeDescriptionService codes;

    public ContactChannelChangeServiceImpl(
            PolicyChangeDao dao,
            PolicyChangeSupportService support,
            CodeDescriptionService codes
    ) {
        this.dao = dao;
        this.support = support;
        this.codes = codes;
    }

    @Override
    @Transactional
    public AddressChangeDto save(String changeCaseNo, String channel, ContactChannelChangeRequest request) {
        requireText(changeCaseNo, "changeCaseNo");
        support.requirePolicy(request.getPolicyNo(), request.getPolicySeq());
        String value = normalizeBlank(request.getValue());
        validateValue(channel, value);

        String itemCode = itemCode(channel);
        String contactId = normalizeBlank(request.getContactId());
        String fieldName;
        String before = null;
        if ("email".equals(channel)) {
            fieldName = "email_address";
            if (contactId != null) {
                PolicyEmail email = dao.findEmail(request.getPolicyNo(), request.getPolicySeq(), contactId);
                if (email == null) throw new NoSuchElementException("找不到電子郵件資料");
                before = normalizeBlank(email.getEmailAddress());
            }
        } else {
            fieldName = "phone_number";
            if (contactId != null) {
                PolicyPhone phone = dao.findPhone(request.getPolicyNo(), request.getPolicySeq(), contactId);
                if (phone == null || !phoneType(channel).equals(phone.getPhoneTypeCode())) {
                    throw new NoSuchElementException("找不到電話資料");
                }
                before = normalizeBlank(phone.getPhoneNumber());
            }
        }
        // 查無既有聯絡資料時，由後端建立穩定 ID；覆核通過前只存在異動草稿。
        if (contactId == null) contactId = UuidV7.next();

        support.validateChangeCaseAccess(request.getPolicyNo(), request.getPolicySeq(), changeCaseNo, itemCode);
        dao.deleteChangeFieldsByItemAndKey(
                request.getPolicyNo(), request.getPolicySeq(), changeCaseNo, itemCode, contactId
        );
        if (Objects.equals(before, value)) {
            support.removeEmptyChangeItemAndAcceptance(
                    request.getPolicyNo(), request.getPolicySeq(), changeCaseNo, itemCode
            );
            return result(changeCaseNo, itemCode, 0);
        }
        support.ensureChangeCaseSaved(request.getPolicyNo(), request.getPolicySeq(), changeCaseNo, itemCode);
        support.upsertFieldChange(
                request.getPolicyNo(), request.getPolicySeq(), changeCaseNo, itemCode,
                new FieldChange(fieldName, contactId, before, value)
        );
        return result(changeCaseNo, itemCode, 1);
    }

    private String itemCode(String channel) {
        return switch (channel) {
            case "email" -> codes.emailChangeItemCode();
            case "telephone" -> codes.telephoneChangeItemCode();
            case "mobile" -> codes.mobileChangeItemCode();
            default -> throw new IllegalArgumentException("不支援的聯絡方式");
        };
    }

    private String phoneType(String channel) {
        return "telephone".equals(channel) ? "11" : "12";
    }

    private void validateValue(String channel, String value) {
        requireText(value, "value");
        if ("email".equals(channel) && !value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("電子郵件格式錯誤");
        }
        if (!"email".equals(channel) && !value.matches("^[0-9+()#\\- ]{6,24}$")) {
            throw new IllegalArgumentException("電話格式錯誤");
        }
    }

    private AddressChangeDto result(String changeCaseNo, String itemCode, int count) {
        return AddressChangeDto.builder()
                .changeCaseNo(changeCaseNo)
                .changeItemCode(itemCode)
                .changedFieldCount(count)
                .build();
    }
}
