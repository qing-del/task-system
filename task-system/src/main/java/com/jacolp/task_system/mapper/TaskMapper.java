package com.jacolp.task_system.mapper;

import com.jacolp.task_system.entity.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {
    /**
     * 插入新的任务（常规插入）
     * @param task 任务
     * @return 是否插入成功
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    boolean insertTask(Task task);

    /**
     * 更新任务完成状态
     * @param task 传入需要更新的任务id和状态
     * @return 是否更新成功
     */
    @Update("update task set status=#{status} where id = #{id}")
    boolean updateTaskStatus(Task task);

    /**
     * 通过id查询任务
     * @param id 需要查询任务的id
     * @return 查询的任务
     */
    @Select("select * from task where id=#{id}")
    Task selectById(long id);

    /**
     * 插入 AI 生成的任务
     * @param task AI生成的任务
     * @return 是否插入成功
     */
    // 如果想拿到数据库自动生成的 ID，加上下面这行
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Task task);


    /**
     * 通过用户id查询任务（分页查询）
     * 如果传入的 limit 为 0 则返回所有任务
     * @param userId 用户id
     * @return 用户的任务列表
     */
    List<Task> selectByUserId(long userId);

    /**
     * 通过任务条件查询任务
     * @param task 任务条件
     * @return 任务列表
     */
    List<Task> selectByTaskCondition(Task task);

    /**
     * 通过任务 id 来更新任务
     *
     */
    @Update("update task set title=#{title}, description=#{description}, exp_reward=#{expReward}, " +
            "reward_type=#{rewardType}, reward=#{reward}, status=#{status}, coin_reward=#{coinReward} where id=#{id}")
    int updateById(Task task);

    /**
     * 根据 id 删除任务
     * @param id 任务 id
     * @return 是否删除成功
     */
    @Delete("delete from task where id=#{id}")
    int deleteById(Long id);
}
