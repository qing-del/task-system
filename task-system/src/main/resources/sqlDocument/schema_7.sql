-- 1. 添加 operator_log 表用于记录操作日志
create table operation_log (
    id bigint auto_increment primary key comment 'id',
    user_id bigint comment '用户id',
    time datetime default current_timestamp comment '操作时间',
    class_name varchar(100) comment '操作类名',
    method_name varchar(100) comment '操作方法名',
    params text comment '操作参数',
    return_value text comment '返回值（出现异常则为null）',
    exception text comment '异常信息（没有异常则为null）',
    cost_time bigint comment '操作耗时（毫秒）'
) engine = InnoDB default charset = utf8mb4 comment = '操作日志表';