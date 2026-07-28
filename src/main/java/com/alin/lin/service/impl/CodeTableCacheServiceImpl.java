package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.entity.CodeDescription;
import com.alin.lin.service.CodeTableCacheService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeTableCacheServiceImpl implements CodeTableCacheService {
    private final PolicyChangeDao policyChangeDao;

    public CodeTableCacheServiceImpl(PolicyChangeDao policyChangeDao) {
        this.policyChangeDao = policyChangeDao;
    }

    @Override
    @Cacheable(cacheNames = "codeTableCodes", key = "#codeGroup + '|' + #codeField")
    public List<CodeDescription> findCodes(String codeGroup, String codeField) {
        return policyChangeDao.findCodes(codeGroup, codeField);
    }

    @Override
    @Cacheable(cacheNames = "codeTableCode", key = "#codeGroup + '|' + #codeField + '|' + #codeBefore")
    public CodeDescription findCode(String codeGroup, String codeField, String codeBefore) {
        return policyChangeDao.findCode(codeGroup, codeField, codeBefore);
    }

    @Override
    @Cacheable(cacheNames = "codeTableCodesByGroup", key = "#codeGroup")
    public List<CodeDescription> findCodesByGroup(String codeGroup) {
        return policyChangeDao.findCodesByGroup(codeGroup);
    }
}
