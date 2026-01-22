package com.jacolp.task_system.service;

import com.jacolp.task_system.dto.InfoResponse;
import com.jacolp.task_system.dto.PageResult;
import com.jacolp.task_system.entity.Task;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TaskService {
    /**
     * 完成任务并结算奖励
     * @param playerId 玩家id
     * @param taskId 任务id
     * @return 是否完成成功
     */
    Boolean completeTask(Long playerId, Long taskId);

    /**
     * 获取玩家任务列表
     * @param playerId 玩家id
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 玩家任务列表
     */
    PageResult<Task> getTaskList(Long playerId, int pageNum, int pageSize);

    /**
     * 插入任务
     * 内部自带设置任务状态为未完成
     * @param task 任务
     * @return 是否插入成功
     */
    boolean insertTask(Task task);


    /**
     * 根据条件获取任务列表
     * @param condition 条件
     * @return 任务列表
     */
    PageResult<Task> getTaskListByCondition(Task condition, int pageNum, int pageSize);

    /**
     * 更新任务逻辑处理
     * 根据任务 ID 来进行更新
     * @param task 传入需要更新的任务
     * @return 是否更新成功
     */
    InfoResponse updateTask(Task task);

    /**
     * 根据任务 id 删除任务
     * @param id 任务 id
     * @return 是否删除成功
     */
    InfoResponse deleteById(Long id);
}
