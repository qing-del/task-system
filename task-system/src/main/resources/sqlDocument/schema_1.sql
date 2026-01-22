create table player_status (
    id bigint primary key auto_increment comment 'id',
    spirit int default 1 comment '精神属性',
    body int default 1 comment '肉体属性',
    current_exp bigint default 0 comment '当前经验',
    total_exp bigint default 0 comment '总经验',
    level int default 1 comment '等级',
    update_time datetime default current_timestamp on update  current_timestamp comment '更新时间'
) ENGINE = InnoDB comment '玩家状态';

INSERT INTO `player_status` (id, spirit, body, current_exp, total_exp, level) VALUES (1, 1, 1, 0, 0, 1);

create table task (
    id bigint primary key auto_increment comment 'id',
    title varchar(100) not null comment '任务标题',
    description text comment '任务描述',
    exp_reward bigint default 0 comment '经验奖励',
    reward_type int default 0 comment '奖励类型: 0-精神，1-肉体',
    reward int default 0 comment '属性点奖励数值',
    status int default 0 comment '任务状态: 0-未完成，1-已完成',
    create_time datetime default current_timestamp comment '创建时间'
) engine = InnoDB comment '任务列表';

INSERT INTO task (id, title, exp_reward, reward_type, reward, status) VALUES (1, '深蹲测试', 50, 1, 5, 0);