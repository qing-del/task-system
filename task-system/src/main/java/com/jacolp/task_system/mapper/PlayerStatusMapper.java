package com.jacolp.task_system.mapper;

import com.jacolp.task_system.entity.PlayerStatus;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PlayerStatusMapper {
    /**
     * 根据id查询玩家状态
     * @param id 需要查询的id
     * @return 查询的玩家状态
     */
    @Select("select * from player_status where id = #{id}")
    PlayerStatus selectById(long id);

    /**
     * 更新玩家状态
     * @param status 玩家状态表
     * @return 如果不为0即为更新成功
     */
    @Update("update player_status set spirit=#{spirit}, body=#{body}, current_exp=#{currentExp}," +
            " total_exp=#{totalExp}, level=#{level}, coin=#{coin} where id = #{id}")
    int updateStatus(PlayerStatus status);

    /**
     * 引入 JWT 令牌之后更新的操作
     * 根据用户 ID 查找玩家状态
     * @param userId 传入用户ID
     * @return 返回用户ID对应的玩家数据
     */
    @Select("select * from player_status where user_id=#{user_id}")
    PlayerStatus selectByUserId(Long userId);

    /**
     * 注册的时候新增新的玩家状态
     * 注释 @Options 用户回填自增的主键 ID 到 user 对象里
     * @param status 传入带UserId的玩家状态表
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void registerInsert(PlayerStatus status);
}
