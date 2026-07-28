-- 舊版啟動腳本曾把未展開的環境變數字串當成實際 user_id 寫入資料庫。
-- 這類帳號不可登入，也不應出現在使用者授權畫面；先清除子檔再清除主檔。
DELETE FROM user_screen_authorization
WHERE user_id LIKE CONCAT('$', '{POS_%_USERNAME}');

DELETE FROM user_role_assignment
WHERE user_id LIKE CONCAT('$', '{POS_%_USERNAME}');

DELETE FROM user_account
WHERE user_id LIKE CONCAT('$', '{POS_%_USERNAME}');
