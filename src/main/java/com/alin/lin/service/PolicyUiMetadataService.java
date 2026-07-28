package com.alin.lin.service;

import com.alin.lin.dto.UiFieldDefinition;

import java.util.List;
import java.util.Map;

/** 提供保單維護畫面的欄位結構、資料容量與動態驗證規則。 */
public interface PolicyUiMetadataService {
    List<UiFieldDefinition> fields(String entity);

    void validate(String entity, Map<String, ?> values);
}
