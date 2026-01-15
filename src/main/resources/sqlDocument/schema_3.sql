-- 1. 给 task 表增加 user_id 字段
alter table task
add column user_id bigint comment '归属用户id';

-- 2. 加上外键约束保证数据完整性
alter table task
add constraint fk_task_user
foreign key (user_id) references user(id);

-- 3. 将所有数据归属到测试账号（admin）
update task set user_id = 1 where user_id is null;