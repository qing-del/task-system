package com.jacolp.task_system.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jacolp.task_system.dto.InfoResponse;
import com.jacolp.task_system.dto.PageResult;
import com.jacolp.task_system.entity.PlayerStatus;
import com.jacolp.task_system.entity.Task;
import com.jacolp.task_system.exception.TaskException;
import com.jacolp.task_system.mapper.PlayerStatusMapper;
import com.jacolp.task_system.mapper.TaskMapper;
import com.jacolp.task_system.service.PlayerStatusService;
import com.jacolp.task_system.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {
    static final int TASK_STATUS_COMPLETE = 1;
    static final int TASK_STATUS_NOT_COMPLETE = 0;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private PlayerStatusMapper playerStatusMapper;

    /**
     * 完成任务逻辑实现
     * @param playerId 玩家id
     * @param taskId 任务id
     * @return 是否完成成功
     */
    @Override
    @Transactional  // 开启事务
    public Boolean completeTask(Long playerId, Long taskId) {
        // 1. 获取任务信息
        Task task = taskMapper.selectById(taskId);
        if (task == null || task.getStatus() == TASK_STATUS_COMPLETE) {
            log.warn("已完成的任务重复点击完成！");
            return false;  // 若是任务已完成直接返回
        }

        // 2. 获取玩家当前状态
        PlayerStatus status = playerStatusMapper.selectById(playerId);

        // 3. 结算奖励
        // 3.1 经验奖励
        status.setCurrentExp(status.getCurrentExp() + task.getExpReward());
        PlayerStatusService.checkLevelUp(status);   // 检测是否升级

        // 3.2 属性奖励
        switch (task.getRewardType()) {
            case 0:
                status.setSpirit(status.getSpirit() + task.getReward());
                break;
            case 1:
                status.setBody(status.getBody() + task.getReward());
                break;
            default:
                log.error("出现任务非法奖励属性类型异常！");
                throw new TaskException("非法任务奖励点！");
        }

        // 4. 更新数据库
        task.setStatus(TASK_STATUS_COMPLETE);
        taskMapper.updateTaskStatus(task);
        playerStatusMapper.updateStatus(status);
        return true;
    }

    /**
     * 获取玩家任务列表
     * @param playerId 玩家id
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 玩家任务列表
     */
    @Override
    public PageResult<Task> getTaskList(Long playerId, int pageNum, int pageSize) {
        // 1. 设置分页查询参数
        PageHelper.startPage(pageNum, pageSize);

        // 2. 调用 Mapper 接口方法
        List<Task> list = taskMapper.selectByUserId(playerId);

        // 3. 解析并封装结果
        Page<Task> p = (Page<Task>) list;
        return new PageResult<Task>(p.getTotal(), p.getResult());
    }

    @Override
    public boolean insertTask(Task task) {
        if (task.getUserId() == null || task.getUserId() == 0) {
            log.error("新任务空玩家异常！");
            throw new TaskException("任务无效用户ID异常！");
        }
        task.setStatus(TASK_STATUS_NOT_COMPLETE);   // 设置兜底：将任务状态设置为未完成
        return taskMapper.insert(task) > 0;
    }

    @Override
    public PageResult<Task> getTaskListByCondition(Task condition, int pageNum, int pageSize) {
        // 1. 设置分页查询参数
        PageHelper.startPage(pageNum, pageSize);

        // 2. 调用 Mapper 接口方法
        List<Task> list = taskMapper.selectByTaskCondition(condition);

        // 3. 解析并封装结果
        Page<Task> p = (Page<Task>) list;
        return new PageResult<Task>(p.getTotal(), p.getResult());
    }

    /**
     * 更新任务逻辑处理
     * 根据任务 ID 来进行更新
     * @param task 传入需要更新的任务
     * @return 是否更新成功
     */
    @Override
    public InfoResponse updateTask(Task task) {
        // 1. 判断是否是一个合法的任务
        // 1.1 判断 id 是否合法
        if (task.getId() == null || task.getId() == 0) {
            log.error("更新任务ID异常！");
            throw new TaskException("任务ID异常！");
        }

        // 1.2 判断是否有归属玩家
        if (task.getUserId() == null || task.getUserId() == 0) {
            log.error("更新任务归属玩家ID异常！");
            throw new TaskException("任务归属玩家ID异常！");
        }

        return taskMapper.updateById(task) > 0 ?
                new InfoResponse(true, "更新任务成功！")
                : new InfoResponse(false, "更新任务失败！");
    }

    @Override
    public InfoResponse deleteById(Long id) {
        // 1. 检测 id 是否合法
        if (id == null || id == 0) {
            log.error("删除任务ID异常！");
            throw new TaskException("任务ID异常！");
        }

        return taskMapper.deleteById(id) > 0 ?
                new InfoResponse(true, "删除任务成功！")
                : new InfoResponse(false, "删除任务失败！");
    }
}
