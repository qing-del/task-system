package com.jacolp.task_system.mapper;

import com.jacolp.task_system.entity.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper {
    /**
     * 保存操作日志
     * @param record 需要保存的日志
     * @return 插入的行数
     */
    @Insert("insert into operation_log(user_id, time, class_name, method_name, params, return_value, exception, " +
            "cost_time) values (#{userId}, #{time}, #{className}, #{methodName}, #{params}, #{returnValue}, " +
            "#{exception}, #{costTime})")
    int insert(OperationLog record);
}
