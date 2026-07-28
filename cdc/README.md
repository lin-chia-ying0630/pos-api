# 代碼對照 CDC

## 目的與範圍

`main.code_definition` 是代碼對照唯一資料來源。只有覆核完成或 Admin 直接完成後寫入正式表的內容才會進入 CDC；`change_review` 的 `P` 暫存資料不發布成正式代碼事件。

CDC 採 MySQL ROW binlog + Debezium，不在 Spring transaction 完成後自行呼叫 Kafka。這可避免「資料庫成功但事件失敗」或反向不一致，也能捕捉 Flyway、批次及合規的資料庫修正。

## Topic 與事件契約

- Topic：`pos.main.code_definition`
- Key：由 Debezium 產生的複合主鍵 `code_group + code_field + code_before`
- Value：保留 Debezium envelope，包括 `op`、`before`、`after`、`source`、`ts_ms`。
- `op=c/r`：建立或初始快照；`op=u`：異動；`op=d` 後接 tombstone：刪除。
- 消費者必須以 Kafka partition offset 冪等處理，不可假設只投遞一次；同 key 只接受較新的 source position。
- 快取消費者收到 c/u 時 upsert `after`，收到 d/tombstone 時移除 key。未知欄位必須忽略以支援 schema 演進。

## 部署步驟

1. MySQL 開啟 `binlog_format=ROW`、`binlog_row_image=FULL`；本機 compose 已設定並保留 7 天。
2. 由 DBA 建立專用 CDC 帳號，僅給 Debezium 文件要求的 replication/read 權限；密碼放 Secret Manager/K8s Secret，不進版本庫。
3. 將 `code-definition-debezium-connector.json.template` 的 `${...}` 由部署系統以 Secret/ConfigMap 產生，不要提交展開後的 JSON。
4. 將 connector POST 到受保護的 Kafka Connect 管理端點；管理端點不得公開至網際網路。
5. 驗證 initial snapshot 數量與 `SELECT COUNT(*) FROM main.code_definition` 一致，再測試新增、修改、刪除各一筆。
6. 告警 connector FAILED、consumer lag、DLQ/反序列化失敗與 binlog 保留時間不足。落後超過保留期時重建 snapshot，不能跳過事件。

## 安全要求

- CDC 帳號與應用帳號分離，禁止 DDL/DML；Kafka 使用 TLS + ACL，只能寫指定 topic。
- 事件不得加入密碼、token 或不必要個資；目前代碼表只允許代碼與中文說明。
- Debezium/Kafka 映像必須固定版本與 digest，經 SCA/簽章驗證後才可部署。
- 變更 connector 設定需走版本審查；不得使用 `table.include.list=main.*` 擴大擷取範圍。
