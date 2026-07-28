package com.alin.lin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 後端提供給前端的欄位資料契約。
 *
 * <p>後端負責：英文欄位 key、資料型態、最大長度、數字精度／小數位、
 * 是否必填、是否為識別鍵、建立時是否允許輸入，以及選項內容。</p>
 *
 * <p>後端不負責：像素欄寬、字級、間距、換行或響應式版面。
 * 這些視覺規則由前端共用 utility 分類，再交由 SCSS design token 呈現。</p>
 */
@Data
@AllArgsConstructor
public class UiFieldDefinition {
    // API 與資料物件使用的原始英文欄位名稱，也是 CHT-code 的 codeField。
    private String key;
    // 欄位資料型態，例如 text、number、datetime 或 select。
    private String type;
    // 是否為後端資料檢核要求的必填欄位。
    private boolean required;
    // 是否為資料識別鍵；修改與刪除時前端應鎖定，但後端仍需再次驗證。
    private boolean identity;
    // 新增模式是否允許輸入；稽核欄位通常由後端產生，因此設為 false。
    private boolean createEditable;
    // 語意上是否屬於長內容欄位；僅提供版面分類線索，不代表實際像素。
    private boolean wide;
    // 文字欄位可接受的最大長度；畫面欄寬由前端換算，不由 API 傳像素。
    private Integer maxLength;
    // 數字欄位總位數。
    private Integer precision;
    // 數字欄位小數位數。
    private Integer scale;
    // HTML 數字輸入的增量提示；真正合法性仍由後端檢核。
    private String step;
    // 欄位允許的最小值提示；真正合法性仍由後端檢核。
    private String min;
    // 選擇型欄位的合法選項；前端不得自行維護另一份清單。
    private List<String> options;
    // main.code_definition.code_description 的使用者可讀說明，由 API 傳到表單顯示。
    private String description;
}
