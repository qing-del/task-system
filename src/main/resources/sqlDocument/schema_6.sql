-- 1. 给 User 表加入 head_photo 来存放 url
alter table user
add column head_photo varchar(100) default null comment '头像';

-- 将 head_photo 列的长度扩大到 500 (通常 URL 500够用了，求稳可以用 TEXT)
ALTER TABLE user MODIFY COLUMN head_photo VARCHAR(500) COMMENT '用户头像';