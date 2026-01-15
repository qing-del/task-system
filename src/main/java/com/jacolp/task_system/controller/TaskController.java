package com.jacolp.task_system.controller;

import com.jacolp.task_system.dto.InfoResponse;
import com.jacolp.task_system.dto.PageResult;
import com.jacolp.task_system.entity.PlayerStatus;
import com.jacolp.task_system.entity.Task;
import com.jacolp.task_system.exception.TaskException;
import com.jacolp.task_system.mapper.PlayerStatusMapper;
import com.jacolp.task_system.mapper.TaskMapper;
import com.jacolp.task_system.service.AiTaskService;
import com.jacolp.task_system.service.PlayerStatusService;
import com.jacolp.task_system.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController // 告诉 Spring 这是一个数据控制器
@RequestMapping("/task")
@CrossOrigin("*")   // 允许跨域请求
public class TaskController {
//    static final int TASK_STATUS_NOT_COMPLETE = 0;  // 定义任务未完成的状态码

    @Autowired private TaskService taskService;
    @Autowired private AiTaskService aiTaskService;
    @Autowired private PlayerStatusService playerStatusService;
//    @Autowired private TaskMapper taskMapper;
//    @Autowired private UserMapper userMapper;   // 需要引入这个来查 ID

//    /**
//     * 完成任务接口（Jwt之前的不安全版本）
//     * \@param playerId 玩家id
//     * @param taskId 任务id
//     * @return 是否完成成功
//     */
//    /*@PostMapping("/complete")
//    public PlayerStatus completeTask(@RequestParam Long playerId, @RequestParam Long taskId) {
//        // 1. 调用 Service 逻辑
//        boolean result = taskService.completeTask(playerId, taskId);
//
//        // 2. 返回是否提交成功
//        return playerStatusMapper.selectById(playerId);
//    }
//    */

    /**
     * 完成任务接口
     * @param taskId 传入任务id
     * @return 返回最新的玩家状态表
     */
    @PostMapping("/complete")
    public ResponseEntity<PlayerStatus> completeTask(@RequestParam Long taskId) {
        PlayerStatus player = playerStatusService.getCurrentPlayerStatus();
        boolean result = taskService.completeTask(player.getId(), taskId);
        if (!result) throw new TaskException("任务完成失败！");
        // 这里返回最新状态
        return ResponseEntity.ok(playerStatusService.selectByUserId(player.getUserId()));
    }

//    /*@PostMapping("/generate")
//    public Task generateAiTask(@RequestParam Long playerId) {
//        // 1. 查玩家状态
//        PlayerStatus player = playerStatusMapper.selectById(playerId);
//
//        // 2. 呼叫 AI 生成任务
//        Task newTask = aiTaskService.generateTask(player);
//
//        if (newTask != null) {
//            // 3. 补全默认字段并村务数据库
//            newTask.setStatus(TASK_STATUS_NOT_COMPLETE);
//            taskMapper.insert(newTask);
//            return newTask;
//        }
//        return null;
//    }*/

    /**
     * 生成 AI 任务
     * @return 返回生成的任务
     */
    @PostMapping("/generate")
    public ResponseEntity<Task> generateAiTask() {
        PlayerStatus status = playerStatusService.getCurrentPlayerStatus(); // 自动识别

        Task newTask = aiTaskService.generateTask(status);
        if (newTask != null) {
            newTask.setUserId(status.getUserId());
            taskService.insertTask(newTask);
            return ResponseEntity.ok(newTask);
        }
        throw new TaskException("生成任务失败！");
    }

    /**
     * 获取玩家状态（Jwt之前的不安全版本）
     * @param playerId 玩家id
     * @return 玩家状态
    @GetMapping("/status")
    public PlayerStatus getStatus(@RequestParam Long playerId) {
        return playerStatusMapper.selectById(playerId);
    }
    */

//    /**
//     * 获取玩家状态
//     * @return 返回现在用户的玩家状态
//     */
//    @GetMapping("/status")
//    public PlayerStatus getStatus() {
//        return getCurrentPlayerStatus();
//    }

    /**
     * 获取玩家任务列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 玩家任务列表
     */
    @GetMapping("/taskList")
    public ResponseEntity<PageResult<Task>> getTaskList(@RequestParam int pageNum, @RequestParam int pageSize) {
        PlayerStatus status = playerStatusService.getCurrentPlayerStatus();
        PageResult<Task> result = taskService.getTaskList(status.getId(), pageNum, pageSize);
        return ResponseEntity.ok(result);
    }

    /**
     * 创建任务
     * @param task 任务
     * @return 创建结果
     */
    @PostMapping("/create")
    public ResponseEntity<String> createTask(@RequestBody Task task) {
        task.setUserId(playerStatusService.getCurrentPlayerStatus().getUserId());
        boolean result =  taskService.insertTask(task);
        return result ? ResponseEntity.ok("任务创建成功！") : ResponseEntity.badRequest().body("任务创建失败！");
    }

    /**
     * 获取任务列表（带条件）
     * @param condition 条件
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 任务列表
     */
    @PostMapping("/taskListByCondition")
    public ResponseEntity<PageResult<Task>> getTaskListByCondition(@RequestBody Task condition, @RequestParam int pageNum, @RequestParam int pageSize) {
        condition.setUserId(playerStatusService.getCurrentPlayerStatus().getUserId());
        PageResult<Task> result = taskService.getTaskListByCondition(condition, pageNum, pageSize);
        return ResponseEntity.ok(result);
    }

    /**
     * 普通玩家用于编辑任务的接口
     * @param task 传入更新后的任务
     * @return
     */
    @PutMapping("/update")
    public ResponseEntity<InfoResponse> updateTask(@RequestBody Task task) {
        task.setUserId(playerStatusService.getCurrentPlayerStatus().getUserId());
        return ResponseEntity.ok(taskService.updateTask(task));
    }

    /**
     * 根据任务 id 删除任务
     * @param id 任务 id
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<InfoResponse> deleteById(@RequestParam Long id) {
        return ResponseEntity.ok(taskService.deleteById(id));
    }
}
