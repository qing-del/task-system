-- 1. 先新建商品表
create table shop_items(
    id bigint primary key auto_increment comment 'ID',
    name varchar(50) comment '商品名字',
    description text comment '商品描述',
    cost_type int comment '花费类型（0=经验点，1=精神，2=体魄，3=硬币）',
    cost_value bigint comment '价格',
    effect_type int comment '效果类型（0=经验点，1=精神，2=体魄）',
    effect_value bigint comment '效果数值',
    stack bigint default -1 comment '库存（默认-1表示无限）'
) engine = InnoDB comment '商品表';

-- 2. 给玩家状态表中加入”金币“列
alter table player_status
add column coin bigint default 0 comment '金币';

-- 3. 添加一个测试商品
insert into shop_items (name, description, cost_type, cost_value, effect_type, effect_value, stack)
values ('测试商品', '这是一个测试商品', 0, 20, 0, 25, -1);