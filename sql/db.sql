create table expense
(
    id         TEXT not null
        primary key,
    date       TEXT not null,
    amount     TEXT not null,
    type       TEXT not null,
    remark     TEXT,
    sub_remark TEXT,
    created_at TIMESTAMP default CURRENT_TIMESTAMP,
    updated_at TIMESTAMP default CURRENT_TIMESTAMP
);
CREATE TABLE recon
(
    id        INTEGER PRIMARY KEY AUTOINCREMENT,       -- 主键ID
    insert_id TEXT NOT NULL,                           -- 插入人标识(字符串)
    raw       TEXT NOT NULL,                           -- 原始对账数据(JSON 字符串)
    status    TEXT NOT NULL DEFAULT 'INIT',            -- 状态: INIT=待处理, SUCCESS=成功, FAIL=失败
    created   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP  -- 更新时间
);


