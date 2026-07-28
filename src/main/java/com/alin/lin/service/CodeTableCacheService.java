package com.alin.lin.service;

import com.alin.lin.entity.CodeDescription;

import java.util.List;

public interface CodeTableCacheService {
    List<CodeDescription> findCodes(String codeGroup, String codeField);

    CodeDescription findCode(String codeGroup, String codeField, String codeBefore);

    List<CodeDescription> findCodesByGroup(String codeGroup);
}
