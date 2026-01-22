-- 1. 新建用户表
create table if not exists user (
    id bigint auto_increment primary key comment 'id',
    username varchar(50) not null comment '用户名',
    password varchar(100) not null comment '密码',
    created_at datetime default current_timestamp comment '创建时间'
) engine = InnoDB default charset = utf8mb4 comment = '用户表';

-- 2. 改造一下玩家状态表 添加归属权
alter table player_status add column user_id bigint comment '用户id';

-- 3. 数据迁移
-- 因为之前有 ID = 1 的数据，所以我们先创建一个默认用户
insert into user (id, username, password) values (1, 'admin', '123456');

-- 把 现有id=1的 玩家数据归在 admin 名下
update player_status set user_id = 1 where id = 1;

-- 4. 添加外键约束
alter table player_status
add constraint fk_player_status
foreign key (user_id) references user (id);