package com.jacolp.task_system.mapper;

import com.jacolp.task_system.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    /**
     * 通过用户名查找用户
     * @param username 待查找的用户名
     * @return 查找出来的用户
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    /**
     * 插入新用户（注册）
     * 注释 @Options 用户回填自增的主键 ID 到 user 对象里
     * @param user 需要新注册的用户
     * @return 是否注册成功
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 检查用户名是否存在账户
     * @param username 待检查的用户名
     * @return 是否存在
     */
    @Select("select count(*) from user where username=#{username}")
    int countByUsername(String username);

    /**
     * 更新用户信息
     * @param user 待更新的用户
     * @return 是否更新成功
     */
    Integer update(User user);
}
