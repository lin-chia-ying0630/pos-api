# POS API 開發規範

## 中文欄位與驗證資料規則

- 所有畫面欄位中文名稱由 `CHT-code` 動態取得，前端不得為 API 欄位另寫固定中文對照。
- `CHT-code` 查詢僅採用啟用且已完成覆核的資料；顯示名稱優先使用 `code_description`，舊資料才退回 `code_before`。
- Spring Security `UserDetails` 不可直接放入一般方法快取，因驗證完成後密碼會被清除；僅快取不含密碼的功能代碼、角色衍生資料或不可變 DTO。
- MyBatis 映射 Java record 時必須使用明確的建構子欄位與型別映射，尤其不可讓帳號密碼欄位依賴隱含別名推導。

## 共用查詢表格對齊

- 表頭與每筆資料必須共用相同 CSS Grid 欄軌。
- 整張表的最小寬度必須由欄位最低寬度、欄距及左右留白統一計算，不可讓各資料列使用 `max-content` 自行決定寬度。
- 同一欄的文字與數字皆從欄位左緣開始，不可因數字靠右造成上下筆資料或相鄰欄位看似錯位。
- 欄寬統一由 `ScrollableRecordTable` 管理，個別畫面不可再建立不同的對齊規則。

## 覆核中心查詢

- 覆核中心固定支援功能代碼、覆核狀態與主要查詢鍵篩選，所有條件必須同時套用於資料查詢與總筆數查詢。
- 覆核狀態空白表示全部；指定時只允許 `P`、`S`、`C`，並由 Service 正規化及驗證。
- 切換篩選條件後從第 1 頁重新查詢，每頁固定 20 筆且由新到舊排序。

## 保單服務畫面授權

- 查詢保單主檔、地址、主附約分別使用 `MPM00001`、`MPM00002`、`MPM00003`。
- 異動保單主檔、地址、主附約分別使用 `MPM00004`、`MPM00005`、`MPM00006`。
- 每個異動 API 只接受自己實體的功能代碼；不得以單一維護代碼取得三種資料的異動權限。

## Local 與 Docker Code Table

- 系統管理的 `CHT-code`、`main-screen`、`main-navigation`、狀態代碼及 `UI-field-*` 必須由 Flyway migration 維持一致。
- `main-code` 與測試群組可能包含各環境實際操作或測試產生的稽核資料，不得為了筆數一致而互相覆蓋或刪除。
- 比對 Code Table 時需使用 `utf8mb4`，避免 MySQL CLI 將繁體中文顯示成問號而誤判資料毀損。

## 強制資安產碼規則

所有產碼必須遵循根目錄 `目前所有的資安弱點.md`，並以當下最新版 OWASP Top 10、API Security Top 10、ASVS、CWE Top 25 與 NIST SSDF 重查。

- API 預設 deny；驗證登入、角色/功能碼、物件及允許修改欄位，UI 隱藏不算授權。
- `MPM00001` 至 `MPM00003` 是唯讀查詢權限；保單主檔、地址與主附約的 POST／PUT／DELETE 必須要求獨立的 `MPM00004`，覆核操作只能由覆核中心執行。
- Controller 使用專用 Request DTO + Bean Validation；禁止直接接收持久化 Entity，避免 Mass Assignment。
- MyBatis 只用 `#{}`；禁 `${}`、拼接 SQL。動態表名、欄名、排序只能映射程式內 allowlist enum。
- 禁執行/反序列化不可信資料；外部 URL 要 allowlist、timeout、大小與轉址限制。
- 密碼、token、連線字串不得進程式、預設值、log 或錯誤；由 Secret 注入，密碼用 BCrypt。
- 列表分頁；request/file/body、長度、範圍、timeout 有上限；敏感流程另加限流/冪等/唯一鎖。
- Cookie 認證必開 CSRF；僅明確 Authorization header 且無 credentialed CORS 才可停用並註明。
- 對外錯誤用 `ResponseBodyDto`，不回 stack trace/SQL/路徑；log 有 correlation ID、遮罩及 CR/LF 清理。
- 異動遵循狀態機、版本、Maker-Checker、pending lock、append-only audit；安全失敗採 fail-closed。
- 測試含 401/403、物件/欄位越權、邊界、重放/競態、注入、回滾；CI 做 SAST/SCA/secret/IaC/SBOM。

查詢 API 組裝主資料時，`CodeDescription` 中文或分類代碼屬顯示輔助資料；對照缺少不得讓主資料整筆查詢失敗，應回傳原始 key/value 或 `null` 標籤。真正會影響狀態轉換、地址類型或保全項目的寫入流程仍使用嚴格查找，缺少必要代碼時必須 fail-closed，不能自行猜測代碼。

正式欄位重新命名時，必須在同一批 Flyway 盤點並遷移 `code_description.code_group/code_field`；不可只改資料表欄位或 Java enum。至少測試「舊資料升級後可查得」及「缺少輔助對照時主資料仍可顯示」。

地址覆核套用必須相容 V37 前快照的 `addressType`，並在快照 JSON 缺少地址類型時以 `policy_change_record_snapshot.changed_record_key` 還原 `addressTypeCode`。禁止用 `null` 查詢正式地址或將 `null` 串進使用者錯誤訊息；只有 JSON 與資料列鍵都缺少時才視為快照損壞。

`db/local/R__demo_policy.sql` 是 repeatable migration，只能使用 `INSERT IGNORE` 補不存在的種子資料。禁止 `ON DUPLICATE KEY UPDATE` 或無條件 UPDATE 正式保單、地址、主附約，避免每次 local 重啟回復初始值並破壞待覆核案件的 before-value 併發檢查。

表名與欄位名不可混用：正式表為 `policy_change_item`、`policy_change_field`、`policy_change_record_snapshot`，其欄位才是 `change_item_code`、`changed_field_name`、`changed_record_type`。修改 Mapper 前必須同時核對 DD、Flyway 最終 schema 與所有 SQL 引用，不可只依 Java property 猜表名。

## 代碼對照 CDC

- `code_definition` 是唯一正式來源，CDC 固定採 MySQL ROW binlog + Debezium，設定與契約見 `cdc/README.md`。
- 禁止在 Service/Controller 交易後自行 publish 代碼事件；否則會產生 dual-write 不一致。只有正式表完成的 `S` 異動才由 binlog 發布，`change_review=P` 不可當正式代碼事件。
- Debezium 事件保留 before/after/op/source；消費者按主鍵與 offset 冪等 upsert/delete，必須能重播 initial snapshot、處理 tombstone 與忽略新增欄位。
- 新增/改名代碼欄位或資料表時，同步更新 Flyway、`table.include.list`、事件契約、相容性測試、監控與 README。
- CDC 帳號採最小權限並從 Secret 注入；Kafka/Connect 強制 TLS、ACL、固定映像 digest，不得公開管理端點。

## 本機登入帳號

- `application-local.properties` 不得內建使用者 ID 或密碼；既有帳號一律由 `user_account`、`user_role_assignment` 與使用者畫面授權表讀取。
- 啟動時補建身分只能透過成對的 `POS_*_USERNAME`／`POS_*_PASSWORD` 環境變數；密碼至少 12 字元。只設定單邊或不符合密碼規則時必須停止啟動並指出實際變數名稱。
- 未提供補建變數不代表停用安全機制，也不得自動建立共用弱密碼帳號；本機仍使用資料庫既有帳號登入。
- 密碼預設雜湊必須使用 `Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()`，並透過
  `DelegatingPasswordEncoder` 寫入 `{argon2@SpringSecurity_v5_8}` 識別前綴。不得再於
  Controller、Service 或設定類別各自建立 BCrypt encoder。
- 既有無前綴 `$2a$`／`$2y$` BCrypt 資料只作為遷移期登入相容；禁止因演算法升級要求使用者
  明碼或嘗試反解。新建、Admin 重設及環境變數補建都必須寫入 Argon2id。
- SPA 登入錯誤必須由 BasicAuthenticationFilter 與全域 exception handling 共用
  `ResponseBodyDto` JSON authentication entry point；禁止回傳 `WWW-Authenticate`，
  避免觸發瀏覽器原生登入視窗。

`main-code` 的「代碼建置」索引由 Flyway V23 維護，勿在前端硬編碼。

保單主檔、保單地址與保單主附約的英文欄位 key、型態、必填、主鍵、選項與資料容量由 `/api/policy-ui-metadata/{entity}` 集中提供；前端頁面不得另存欄位清單。中文名稱由 `CHT-code` 的 `codeField -> codeBefore` 提供，查無對照時保留 API 回傳的英文 key。像素欄寬屬於前端呈現責任，由共用表格元件集中管理，不由 API 回傳。

後端 metadata 只負責資料契約：`key`、`type`、`maxLength`、`precision`、`scale`、必填、識別鍵、建立時可編輯性與合法選項。禁止由後端回傳像素欄寬、字級、間距或響應式規則；前端依 metadata 分成欄寬等級，再由 SCSS design token 決定實際尺寸。前端檢核僅改善操作體驗，後端仍須執行最終資料檢核與授權。

保單維護 metadata 的型別、長度／精度及使用者說明必須存於 `main.code_definition` 的 `UI-field-master`、`UI-field-address`、`UI-field-ride`。後端只對已建立且啟用、覆核完成的設定執行動態檢查；缺少設定即略過，不可讓 metadata API 或網站失敗。`code_description` 必須是可直接由 API 顯示的繁體中文說明，不得存放程式旗標。

壽險欄位容量以根目錄 DD 為基準；擴檔必須使用新 Flyway 版本，同步修改父表、全部 FK 子表、Bean Validation、業務 Validator、`UI-field-*` metadata、前端查詢限制及文件。現行基準為 `policy_no VARCHAR(20)`、`address_type_code VARCHAR(8)`、地址／聯絡內容 300 字、`coverage_item_seq VARCHAR(10)`、`product_code VARCHAR(32)`、`insured_amount DECIMAL(18,2)`、`premium_amount DECIMAL(18,4)`。

「擴檔」包含欄位內容，不等於只加大 VARCHAR。核心檔至少依 V50 保存：契約狀態／日期／期間／繳別／商品／要保書／客戶／業務員，聯絡檔保存獨立 postalCode、addressText、emailAddress、telephoneNo、mobileNo，保障項目保存商品名稱、主約商品、繳別、繳費期間及生效／終止日。新增欄位必須同步 Entity、Request DTO allowlist、Mapper insert/update、覆核 before/after 快照、Applier、metadata、CHT-code、前端型別及測試。

資料遷移只能依明確的地址用途代碼拆分地址、Email 與電話；禁止依字串外觀猜測資料種類。

## UUID 與聯絡資料正規化

重新設計後依根目錄 `壽險資料ID設計.md`：技術 ID 統一由後端產生 UUID v7，資料庫仍須設
PRIMARY KEY／UNIQUE；新增 API 不接受前端指定 ID。地址、Email、電話必須分表，禁止恢復
`fullWidthAddress/halfWidthAddress` 混放。一次保全案件可以送出多個異動項目，但每個項目以
`changeItemCode + recordKey` 獨立防重、快照及覆核。

V52 之後新增保單契約、地址、保障項目、案件、異動項目、欄位異動、快照、覆核或代碼定義時，
必須先由後端 `UuidV7.next()` 產生對應 ID；不得使用前端提交值，也不得恢復以流水號作為
API／稽核 recordKey。

共用 API 的畫面授權必須列出每個合法呼叫來源：保單總覽允許 `MPS00001` 與 `MPM00001`～`MPM00003`，保全案件查詢／明細允許 `MPS00002` 或 `MPS00003`，郵遞區號及申請資格只允許 `MPS00001`。新增共用端點時必須同步補 `ScreenAuthorizationFilterTest`。

`user_screen_authorization` 的覆核 `content_before`／`content_after` 必須保存每筆資料列的全部欄位：`userId`、`functionCode`、`createdBy`、`createdAt`、`updatedBy`、`updatedAt`，結構固定為 `{ "rows": [...] }`；不得退化為只有 `key` 與 `functionCodes` 的彙總快照。

使用者授權的「新增」必須真正建立 `user_account`，不可只從既有帳號下拉選取。建立 API 使用專用 Request DTO，驗證使用者 ID、12 至 128 字元初始密碼、啟用狀態與角色 allowlist；密碼只以 BCrypt 雜湊保存。修改帳號使用另一個 Request DTO，只能修改 enabled 與完整角色集合，禁止變更 userId、回傳密碼、停用自己、移除自己的 ADMIN 角色，或停用最後一位有效 Admin。密碼重設使用獨立 PATCH API，覆核內容只保存 `passwordReset` 行為旗標，禁止保存任何密碼值。帳號與畫面授權查詢 DTO 必須回傳 `createdBy`、`createdAt`、`updatedBy`、`updatedAt`，供授權頁與覆核中心呈現稽核責任。

上述三種資料的新增、修改、刪除皆建立 `change_review` 待覆核資料；來源頁不得直接決策，統一由覆核中心 API 確認或取消，並在同一交易同步來源資料的 `review_status`。

## 目的

本檔提供後續修改 `pos-api` 時必須維持的架構與交易規則。重新設計流程、資料表或分層後，需同步更新本檔與 `readme.md`。

## 技術棧

- Java 17、Spring Boot、Spring Security。
- MyBatis、MySQL、Flyway。
- Bean Validation、Lombok。
- JUnit 5、Mockito、Testcontainers。
- Maven、Docker、GitHub Actions。

## 三層架構

### Controller

- 只處理路由、path/body 參數、`@Valid`、`@Validated` 與回覆包裝。
- 所有回覆包含 Spring Security 的 `401/403`，都必須符合 `ResponseBodyDto<T>`。
- 每支 API 保留一行中文註解，說明對應畫面與使用時機。

### Service

- 每個 use case 都有 interface 與 implementation。
- `PolicyChangeServiceImpl` 只做 facade 委派，不重複商業邏輯。
- 目前 use case：Query、Draft、Address Save、Amount Save、Review、Apply。
- 跨多張表的儲存或覆核必須使用 `@Transactional`。
- 正規化與純欄位差異比對放 `PolicyChangeFieldUtil`。
- `SecurityConfig` 只負責組態與 Bean wiring，不得直接查表、寫 SQL 或重建角色；帳號同步必須經 `UserAccountSecurityService`。
- Controller 或跨 use case 注入的正式應用服務在 `service` 定義 interface，在 `service.impl` 放 Spring 實作；目前包含 `PolicyUiMetadataService` 與 `ChangeReviewApplier`。
- 單一實作的領域協作者不強制建立空 interface：Validator 放 `service.validation`、流程 Guard 放 `service.support`、執行 Policy 放 `service.policy`，不得與對外 Service interface 混放。

### DAO

- `PolicyChangeDao` 是 MyBatis mapper interface，由 MyBatis 直接建立代理。
- SQL 放在 `PolicyChangeDao.xml`，namespace 必須是 `com.alin.lin.dao.PolicyChangeDao`。
- 不建立只逐項轉呼叫的 `PolicyChangeDaoImpl` 或另一份 Mapper interface。
- 寫入方法回傳 affected row count；狀態轉換與重要更新必須檢查筆數。
- 單表 row 使用 Entity；join、聚合、畫面組合或操作結果才使用 DTO。

## 快取規則

- 查詢快取只放在 public Service 方法；Controller、Filter、Config 與 DAO 不加業務快取。
- `@Cacheable` key 必須由穩定業務鍵組成，例如 `userId` 或 `HTTP method|requestPath`。
- `@CacheEvict` 不得放在 private/self-invocation helper，Spring AOP 不會攔截；必須放在外部呼叫的 public 異動方法。
- 使用者密碼、啟用狀態、角色異動必須清除 `userSecurityDetails`；畫面授權異動必須清除 `userFunctionCodes`。
- `code_definition` 的異動或覆核可能影響 `CHT-code`、`main-screen`、`api-screen-authorization`，必須同步清除 `codeTableCodes`、`codeTableCode`、`codeTableCodesByGroup`、`availableFunctionCodes`、`apiFunctionCodes`。
- 只在交易成功後清除快取；異動失敗或回滾時不得讓快取先失效。快取不得取代資料庫唯一鍵、狀態鎖與 API 權限判斷。

## DTO 與 Entity

- 每張 SQL table 都要有一個中文註解完整的 Entity。
- `@RequestBody` 一律使用 `*Request` DTO，不直接使用 Entity。
- Request 必填欄位使用 Bean Validation。
- DTO 與 Entity 預設使用：

```java
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
```

- API 外層只使用 `ResponseBodyDto<T>`；request 不包回覆外層。

## 案號

- 格式：`C + 民國年 + 月日 + 至少三碼流水號`。
- 必須由 `policy_change_case_sequence` 使用單一原子 `INSERT ... ON DUPLICATE KEY UPDATE` 與 connection-local `LAST_INSERT_ID()` 取號。
- 禁止使用 JVM 記憶體、自增欄位快取或只查 `MAX(change_case_no)`。
- 取號不建立受理檔，只有真的異動才建立 `P` 草稿。
- 取號前必須依 `policy_no + policy_seq + change_item` 查最近一筆已受理案件；最近狀態為 `P` 時回覆 HTTP 409 與「此保單正在受理中，無法申請」。eligibility API 與建案 Service 必須執行相同規則。
- 取號必須新增一筆 `policy_change_case_reservation`，並以 `policy_change_case_reservation_item` 保存一至多個勾選項目；儲存前驗證保單、擁有者、期限與項目是否在預約清單。
- 同一案號只能有一筆 `policy_change_acceptance`，但可以依序新增多筆 `policy_change_item`；第二項儲存不得再次建立受理檔或拒絕既有案號。
- 流水號不可限制在 `999`；`String.format("%03d", serial)` 只定義最小寬度。

## 草稿

- `policy_change_field` 的有效草稿鍵為案號、項目、欄位、`change_key`。
- `policy_change_file` 的有效草稿鍵為案號、項目、檔案、`change_key`。
- 同一目標重複儲存要替換最新草稿，不能累積多筆有效資料。
- 使用者改回主檔原值時要刪除該目標草稿。
- 項目已無欄位或檔案時刪除 `policy_change_item`；案件已無項目時刪除 `P` 受理資料。
- 若未來需要編輯歷程，另建立 revision/history，不可把歷程當成目前有效草稿。

## 覆核

- 完成流程固定為 `P -> A -> 套用 -> S`，取消為 `P -> C`。
- `P -> A/C` 必須使用條件更新並檢查 affected row count，避免重複覆核。
- 套用前以 `SELECT ... FOR UPDATE` 鎖定資料列，再確認目前值仍等於草稿 `content_before`。
- 主附約一律先鎖主檔、再依 `ride_order` 鎖附約；地址依 `change_key` 排序鎖定，禁止反向鎖順序。
- 每個主檔、地址或附約 UPDATE 都必須檢查 affected row count 為 1。
- 主檔已被其他案件修改時拋出 `ChangeCaseConflictException` 並回覆 HTTP 409。
- 覆核清單之外必須提供案件明細 API，包含 `changeFields` 與 `changeFiles`。
- 完成、主檔套用、總保費重算與狀態更新必須在同一交易內。
- 受理檔必須保存 `created_by`、`reviewed_by`、`reviewed_at`；啟用 Security 時禁止建檔人覆核自己的案件。

## 變更項目

- `changeItemCodes` 不設定固定筆數上限；只驗證至少一項、代碼格式正確且同一案號內不可重複。

### 001 地址與聯絡資料

- `01/02` 使用郵遞區號與地址。
- 其他型態使用 `email / 電話 / 手機`。
- 地址快照 `policy_change_file.change_key = address_type`。
- 地址欄位 `policy_change_field.change_key = address_type`。
- 未修改或改回原值時 `changedFieldCount = 0`，且不可保留舊草稿。

### 002 主約保額

- 案件清單聚合變更項目時必須去重。
- `main_policy_master` 不保存主約險種、年期與保額。
- 002 只記錄並更新 `main_policy_ride` 的主約列，`ride_order = 000`、`change_key = 000`。
- 002 同時保存 `main_policy_ride` 完整資料列快照；欄位紀錄負責套用，檔案快照負責查詢呈現。
- 查詢明細需將 snake_case 或複合欄位名稱轉為對應 JSON key，再由 `CHT-code` 補入 `changeFields.chineseName`。
- `changedFieldCount` 回傳業務異動數 `1`。

### 003 附約保額

- request 必須包含 `coverageItemSeq`。
- 003 只能修改 `coverage_item_type = RIDER` 且 `coverage_item_seq != 000` 的資料；主約 `BASE/000` 只能由 002 修改。
- 每筆欄位的 `change_key = coverageItemSeq`。
- request 內不可有重複 `coverageItemSeq`；Service 必須保留最終驗證，不可只依賴前端篩選。

### 004／005／006 聯絡資料

- contactId 有值時更新既有 email／電話；contactId 空白時由後端產生 UUIDv7，先以 `contentBefore = null` 保存新增草稿。
- 新增資料不得在送件時直接寫正式聯絡表；覆核通過後才依項目代碼新增 email、市內電話 `11` 或行動電話 `12`。

## CodeDescription

- 地址型態、變更項目、受理狀態與主附約型態由 `code_description` 管理。
- Java 判斷使用穩定 code key，不使用中文描述。
- 覆核快照欄位名稱使用 `codeGroup=CHT-code`、`codeField=JSON key`、`codeBefore=中文名稱`；案件明細 API 將 JSON 拆成 `snapshotFields`，逐欄回傳中文名稱與異動前後值。
- `CodeTable` 定義 group/field；`CodeDescriptionMeaning` 定義程式需要的穩定 key。
- 欄位名稱、regex、組合與解析規則才放 enum。

## DD 與壽險欄位命名

- 欄位命名與中文定義以專案根目錄 `欄位命名規則ＤＤ定義.md` 為唯一查找入口；需要新增或判斷欄位名稱時必須先查此檔。企業正式壽險 DD 與本文件不同時，以企業 DD 為準並同步更新本文件。
- 既有欄位改名或資料遷移前，必須再查專案根目錄 `專案欄位命名盤點.md`，依 P0／P1／P2 順序處理並保護歷史覆核快照與 API 相容性。
- 新增或調整資料表時，必須先查 `欄位命名規則ＤＤ定義.md` 的「壽險邏輯資料表命名」；資料表採單數 snake_case，使用業務實體名稱，不新增 `main_*`、`*_master` 或錯用 `ride`。
- 實作保全或覆核前必須查 DD 的「壽險共通業務邏輯」：Maker 提交只寫 review staging，正式表只能在覆核 S 的同一交易套用；C 不得改變正式資料。
- DB 使用 snake_case，Java／JSON／TypeScript 使用 camelCase，畫面使用繁體中文壽險用語；跨來源查詢參數 `key1` 不得成為實體資料欄位名稱。
- canonical 資料表與欄位已使用 `policy_coverage`、`coverage_item_*`、`coverage_term_years`；舊 `/rides` route 與部分 Java 方法僅作相容 alias，新功能一律使用 coverage 命名。`customer_id`、`agent_code` 應在客戶／通路模組進入專案時依 DD 建模，不得先塞入保單備註欄位。

## Security 與設定

- CORS origin 從 `pos.cors.allowed-origins`／`CORS_ALLOWED_ORIGINS` 取得，不可硬寫在 Controller。
- Security 採 fail-closed，所有 profile 預設開啟；只有 `local`／`test` 可明確關閉，其他 profile 關閉時必須拒絕啟動。
- `prod` 必須設定 `pos.security.require-https=true`，並由可信任反向代理傳入 HTTPS forwarding header。
- MAKER 可新增與儲存；REVIEWER 才能覆核。
- MAKER 只能修改與查看自己建立的案件，REVIEWER 可查看覆核清單；Service 層也必須重做案件擁有者檢查。
- 一個 `userId` 可在 `user_role_assignment` 擁有兩個以上角色；登入 API 必須回傳全部角色，前後端權限取聯集。環境變數若以相同帳號設定多個角色，必須使用相同密碼並合併角色，不得因應用重啟覆蓋資料庫既有角色。密碼至少 12 個字元。
- 多角色不會解除職務分離規則：即使同時具有 `MAKER` 與 `REVIEWER`，仍不得覆核自己建立的案件。
- 內建帳號角色組合固定為 `maker = ROLE_MAKER + ROLE_USER`、`reviewer = ROLE_REVIEWER + ROLE_ADMIN`；資料庫仍可為相同 `userId` 增加更多角色。
- 使用者授權的查詢 API 開放給 `ROLE_USER` 與 `ROLE_ADMIN`；新增與修改角色集合只能由 `ROLE_ADMIN` 執行。新增代表替既有 `users.username` 合併角色，修改代表取代完整角色集合，不在此功能建立帳號或變更密碼。
- 使用者授權異動不送覆核；寫入 `user_role_assignment` 後必須在同一交易建立 `MUS00001` 的 `change_review` 與 `change_review_audit`，狀態直接為 `S`，保存異動前後角色、操作人與時間。任何一段失敗皆須回滾。
- 新增或修改使用者角色前，必須以 `MUS00001 + userId` 作為相同業務 Key，使用 `FOR UPDATE` 查詢是否已有 `P` 狀態資料；若已在佇列中則回覆 409，禁止建立重複異動。新的直接完成資料仍記為 `S`，不留在待覆核佇列。
- 使用者角色查詢需回傳該 `MUS00001 + userId` 最新稽核狀態；沒有歷程的既有帳號預設為 `S`。狀態原值交給前端，不在後端改寫中文。
- 所有維護頁面的修改 API 都必須經過共用 `PendingReviewGuard`，並取得 `change_review_pending_lock(function_code, unique_key)` 的資料庫唯一鎖；不得只依賴「查不到資料」的 `FOR UPDATE`。
- 新送出的保單、聯絡資料、保障項目及代碼異動使用 `workflowMode=STAGED`；Maker 只寫快照，Reviewer 核准後由 `ChangeReviewApplier` 依 `recordVersion` 套用正式表。既有 `LEGACY` 案件只同步狀態，不可重複套用。
- `ROLE_ADMIN` 維護資料時由 `ReviewExecutionPolicy` 判斷為直接完成：正式異動及 `S / DIRECT_APPLY` 稽核必須在同一交易，仍不得跨過 pending-key lock。
- 畫面授權以 `userId` 直接掛載，不以角色推算；資料庫 `user_screen_authorization` 允許同一 userId 複選多個功能畫面。登入 API 同時回傳 `roles` 與 `functionCodes`。
- 新增或修改角色不得連動 `user_screen_authorization`，應用程式啟動也不得依角色補畫面。畫面只能由 Admin 在畫面授權 Dialog 中針對 userId 手動複選及儲存；既有授權不得因角色變更而被重算。
- 只有 `ROLE_ADMIN` 能替 userId 新增或修改多個角色及多個畫面；`ROLE_USER` 僅能查詢。Admin 修改後立即生效並以 `MUS00001`、狀態 `S` 建立稽核。資料庫及對外 DTO／API 一律使用 `user_id`／`userId`。
- 前端選單與路由授權不構成安全邊界；後端 `ScreenAuthorizationFilter` 必須將 API 對應到功能代碼，再用登入 userId 查 `user_screen_authorization`，沒有畫面授權一律回覆 403。
- DB 與帳號密碼只能由環境變數、Docker secret 或 K8s Secret 提供，程式不得有正式預設密碼。
- K8s 使用 Actuator liveness/readiness endpoint。
- Logback 在 `prod`／K8s 只能輸出 stdout/stderr，交由集中式日誌系統收集；rolling file 只在非 `prod` 啟用，一般技術 Log 不寫入業務資料庫。
- `change_review` 只保存目前覆核狀態；每次送出、核准或拒絕必須在同一交易追加 `change_review_audit`，歷史稽核事件不得更新或刪除。
- 覆核決策必須先鎖定主檔，只允許 `P -> S/C`，禁止建檔人覆核自己的異動；稽核事件保留操作者、前後狀態、必要快照及可用的 request／trace ID。
- 覆核清單允許不帶篩選條件，必須在資料庫端依 `created_at DESC, id DESC` 排序並固定每頁 20 筆，不可回傳無上限清單交由前端分頁。
- `change_review.unique_key` 必須保存來源資料的全部主要 Key，順序固定並以 `|` 分隔，例如主檔為 `policyNo|policySeq`、地址為 `policyNo|policySeq|addressType`、附約為 `policyNo|policySeq|rideOrder`、代碼為 `codeGroup|codeField|codeBefore`。新增資料類型時不得只放部分 Key。
- 覆核中心第一主要 Key 的 API 參數統一命名為 `key1`，不得暴露 `policyNo` 或 `policySeq` 查詢參數。保單資料的 key1 對應 `policy_no`，代碼資料的 key1 對應 `code_group`；查詢亦涵蓋 `unique_key`、案號與快照識別值。
- `change_review.content_before/content_after` 的新資料必須保存標準 JSON，讓前端資料詳細內容 Dialog 可依 `CHT-code` 拆成中文欄位；舊格式只做讀取相容，不得繼續產生。
- Docker runtime 必須非 root；應使用唯讀 root filesystem、`no-new-privileges` 與最小 Linux capabilities。
- Docker 建置與執行映像必須固定 digest；升級時同步掃描弱點並更新 digest。

## Flyway

- 禁止重新加入單一 `schema.sql` 當正式升版工具。
- 已發布 migration 不可修改；新結構建立下一個 `Vn__description.sql`。
- 示範保單放 `db/local`，不可放正式 migration。
- 新增資料表時同步新增 Entity、DAO SQL、測試及 README 資料流說明。

## 測試

- 一般單元測試不得依賴開發者本機 MySQL。
- IntelliJ 本機 Debug 使用 `.run/POS API Local.run.xml`，必須啟用 `local` profile，避免 `db/local` migration 在同一資料庫被 Flyway 判定為 missing。
- Controller/Security slice 至少驗證未登入、角色越權、MAKER/REVIEWER 正常流程與 CORS properties。
- SQL、Flyway、交易與併發流程使用 MySQL Testcontainers。
- 至少覆蓋：原子案號、無異動、重複儲存、改回原值、P/S/C、409 衝突、001/002/003。
- Docker 不可用時整合測試可略過；CI 必須在 Docker 可用環境完整執行。
- 修改 SQL 遮罩規則時同步補 `MaskedSqlLogInterceptorTest`。
- CI 必須執行 CodeQL、OSV、Docker build；Maven、Docker 與 GitHub Actions 由 Dependabot 定期更新。
- 同一業務 Key 的非 `S` 案件由資料庫鎖與 Service 衝突檢查共同保護；MySQL deadlock、lock timeout 等暫態競爭必須在 Service 轉為一致的 409 業務衝突，不得洩漏為 500。
- 覆核快照的金額比較必須使用數值語意，不得以 JSON 字串小數位數判斷不同。

## 兩階段優化準則

- 第一階段先修復可驗證契約：測試、Flyway、登入安全、狀態轉移與併發衝突全部通過後，才進行結構整理。
- 第二階段再抽共用 metadata／版面規則、移除重複設定並更新文件；不可在尚未建立回歸保護時進行大範圍搬移。
- 本機帳密與 DB 密碼必須由環境變數提供，`application-local.properties` 不得含可用預設密碼。
- local profile 可透過 `spring.config.import=optional:file:.env[.properties]` 讀取被 Git 排除的 `.env`；只提交 `.env.example` 欄位範本，不提交真實值。
- 多 Pod 部署時，本機 Caffeine 只可當近端快取；需由 CDC、事件或 Redis pub/sub 廣播失效。外部基礎設施尚未就緒前，不得宣稱單機 eviction 已提供叢集一致性。
- 使用者與畫面授權清單的稽核時間由 Service 資料邊界統一轉為 `LocalDateTime`；必須同時接受 MyBatis／Connector/J 回傳的 `LocalDateTime` 與 `java.sql.Timestamp`，不可在組 DTO 時直接強制轉型。
- migration 必須清除舊版誤寫入的未展開環境變數帳號（例如 `${POS_MAKER_USERNAME}`）；不可讓不可登入的 placeholder 帳號出現在授權清單。

## 驗證指令

```bash
mvn test
mvn clean verify
docker build -t pos-api:latest .
```

- Docker Maven 層使用 BuildKit cache；不要以 `dependency:go-offline` 預抓未使用的 dependency management BOM。
# Deployment rules

- Docker 完整發版程序只維護於 `pos-web/readme.md` 與 `pos-web/compose.yaml`；API README 只保留入口連結，避免兩份推送／啟動指令分歧。
- Keep screen branch and role mapping in `main.code_description` (`main-screen/screen`); do not duplicate the mapping in frontend labels.

- Production uses JDBC-backed `users` and `authorities`; local/test may use in-memory users.
- Never commit passwords, password hashes, `.env` files, or database backups.
- Back up MySQL before every Flyway migration and rehearse V6/V7 against a sanitized copy.
- Keep database constraints and transactional status transitions as the source of truth for concurrent cases.
# Code 清單維護

- Controller：`UserAuthorizationController`
- Service：`CodeDescriptionService`
- DAO：`PolicyChangeDao.findAllCodes`
- Table：`main.code_description`
- 回應：`ResponseBodyDto<List<CodeDescription>>`
- 變更 Mapper 後必須重新啟動後端，前端才能取得最新 API。
- Code 查詢與異動必須拆成不同畫面：`MCM00001` 僅查詢，`MCM00002` 提供新增與修改；異動畫面不得放覆核按鈕，所有決策統一進 `MPS00003` 覆核中心。
- Code 修改鎖定 `codeGroup`、`codeField`，允許變更 `codeBefore` 與其下方內容；request 仍必須攜帶 `originalCodeGroup`、`originalCodeField`、`originalCodeBefore` 定位原資料並檢查新 Key 不重複。
- 畫面功能代碼統一維護於 `main-screen/function_code`，採 `M` 加業務領域與五碼流水號，例如保全 `MPS00001`、保單 `MPM00001`、代碼查詢 `MCM00001`、代碼異動 `MCM00002`、使用者 `MUS00001`；新增畫面必須先建立對照資料，不得自行延用舊式 `M001` 三碼格式。
- `MPS00001` 的畫面名稱固定為「申請保全變更」。變更項目 001～006 必須全部依後端清單產生選項；選取後要有對應的地址、主約、附約、email、市內電話、行動電話異動入口，不得以固定三筆上限截斷。
- 業務 API 與畫面授權不得寫死在 Security Filter。統一由 `api-screen-authorization` code table 設定：`code_field=HTTP method`、`code_before=路徑前綴或 Ant path pattern`、`code_after=允許功能代碼`；多個代碼表示任一授權即可。Filter 僅保留登入、健康檢查等技術例外；新增 API 時必須同步建立 migration 與授權測試。
- `GET /api/function-codes` 提供所有已登入角色讀取畫面功能代碼，供右上角功能標籤使用；前端不得另建完整功能代碼固定對照表。
- `GET /api/navigation-labels` 只提供 `main-navigation/navigation_label`，`GET /api/field-labels` 只提供 `CHT-code`；兩者供一般已登入畫面讀取，不得綁定 `MCM00001`。Router 與表頭只保存英文 key，查無中文時回傳原 key。
- 共用覆核資料必須保存 `source_record_type`、`source_record_id`，以便畫面由覆核資料回查原始異動欄位檔、異動檔案檔或代碼資料。
### 動態欄位中文名稱

- 清單與明細由 API 回傳物件的 key 動態展開，前端不得另建固定中文表頭。
- 所有 API 欄位中文名稱集中建立於 `code_definition`：`code_group='CHT-code'`、`code_field=API key`、`code_before=中文名稱`。
- 新增 DTO 欄位時必須在同一版 Flyway migration 補上 CHT-code；前端找不到對應時保留原始英文 key，方便辨識遺漏的 DD。
- `acceptanceStatusDescription` 顯示為「保全受理狀態說明」，不可在 Vue 元件內寫死。
- `V64__rebuild_canonical_cht_field_labels.sql` 是目前標準中文欄名基準；調整欄位 key 時必須同步更新此基準的後續 migration，不可在 Vue 另加別名。
- 所有 API 清單共用 `ScrollableRecordTable`，欄寬由目前頁面表頭與實際資料內容的視覺長度計算；各頁不得再建立固定 Grid 欄寬或另一套表頭樣式。
