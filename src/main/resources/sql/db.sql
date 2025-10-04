CREATE TABLE IF NOT EXISTS expense
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
create table if not exists recon
(
    id         INTEGER
        primary key autoincrement,
    insert_id  TEXT                     not null,
    raw        TEXT                     not null,
    encrypt_id TEXT,
    status     TEXT      default 'INIT' not null,
    created    TIMESTAMP default CURRENT_TIMESTAMP,
    updated    TIMESTAMP default CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS request_log
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    method     TEXT         NOT NULL,
    path       TEXT         NOT NULL,
    query      TEXT,
    headers    TEXT,
    body       TEXT,
    client_ip  TEXT,
    status     INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


