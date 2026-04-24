-- wx_user 默认昵称回刷脚本
-- 执行前请备份数据库
-- 适用于 PostgreSQL
-- 规则：将空昵称（NULL 或仅空白）更新为「微习惯_8位随机码」
-- 随机字符集：23456789ABCDEFGHJKMNPQRSTUVWXYZ（排除 0/O/1/I/L）

-- 1) 回刷前：待更新数量
SELECT COUNT(*) AS pending_count
FROM wx_user
WHERE COALESCE(BTRIM(nick_name), '') = '';

-- 2) 执行回刷
UPDATE wx_user u
SET nick_name = '微习惯_' || (
    SELECT STRING_AGG(
        SUBSTRING('23456789ABCDEFGHJKMNPQRSTUVWXYZ'
            FROM (
                (GET_BYTE(DECODE(MD5(u.id || ':' || gs::TEXT || ':nick'), 'hex'), 0) % 32) + 1
            )
            FOR 1
        ),
        '' ORDER BY gs
    )
    FROM GENERATE_SERIES(1, 8) AS gs
)
WHERE COALESCE(BTRIM(u.nick_name), '') = '';

-- 3) 回刷后：剩余空昵称数量（预期为 0）
SELECT COUNT(*) AS remaining_blank_count
FROM wx_user
WHERE COALESCE(BTRIM(nick_name), '') = '';

-- 4) 抽样检查（最多 20 条）
SELECT id, nick_name
FROM wx_user
WHERE nick_name LIKE '微习惯_%'
ORDER BY update_time DESC NULLS LAST, create_time DESC NULLS LAST
LIMIT 20;
