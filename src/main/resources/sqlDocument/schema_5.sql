-- 1. 给 task 表加入 coinReward 列
alter table task
add column coin_reward bigint default 0 comment '金币奖励'