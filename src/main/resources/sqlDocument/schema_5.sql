-- 1. 给 task 表加入 coinReward 列
alter table task
add column coin_reward bigint default 0 comment '金币奖励';

-- 2. 给 user 表加入 authority 这一栏作为权限管理
alter table user
add column authority int default 1 comment '权限（0封号，1普通，2贵族，3管理员）';