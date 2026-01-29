package com.jacolp.task_system.controller;

import com.jacolp.task_system.anno.MethodLog;
import com.jacolp.task_system.dto.InfoResponse;
import com.jacolp.task_system.dto.PageResult;
import com.jacolp.task_system.entity.PlayerStatus;
import com.jacolp.task_system.entity.Task;
import com.jacolp.task_system.exception.TaskException;
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

    @Autowired private TaskService taskService;
    @Autowired private AiTaskService aiTaskService;
    @Autowired private PlayerStatusService playerStatusService;

    public TaskController() {
        log.info("TaskController 初始化");
    }

    /**
     * 完成任务接口
     * @param taskId 传入任务id
     * @return 返回最新的玩家状态表
     */
    @MethodLog
    @PostMapping("/complete")
    public ResponseEntity<PlayerStatus> completeTask(@RequestParam Long taskId) {
        PlayerStatus player = playerStatusService.getCurrentPlayerStatus();
        boolean result = taskService.completeTask(player.getId(), taskId);
        if (!result) throw new TaskException("任务完成失败！");
        // 这里返回最新状态
        return ResponseEntity.ok(playerStatusService.selectByUserId(player.getUserId()));
    }

    /**
     * 生成 AI 任务
     * @return 返回生成的任务
     */
    @MethodLog
    @PostMapping("/generate")
    public ResponseEntity<Task> generateAiTask() {
        log.info("Start calling AI generation task...");
        PlayerStatus status = playerStatusService.getCurrentPlayerStatus(); // 自动识别

        Task newTask = aiTaskService.generateTask(status);
        if (newTask != null) {
            newTask.setUserId(status.getUserId());
            taskService.insertTask(newTask);
            return ResponseEntity.ok(newTask);
        }
        log.error("Ai generates an empty task exception!");
        throw new TaskException("生成任务失败！");
    }

    /**
     * 获取玩家任务列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 玩家任务列表
     */
    @MethodLog
    @GetMapping("/taskList")
    public ResponseEntity<PageResult<Task>> getTaskList(@RequestParam int pageNum, @RequestParam int pageSize) {
        log.info("Start retrieving the task list...");
        PlayerStatus status = playerStatusService.getCurrentPlayerStatus();
        PageResult<Task> result = taskService.getTaskList(status.getId(), pageNum, pageSize);
        return ResponseEntity.ok(result);
    }

    /**
     * 创建任务
     * @param task 任务
     * @return 创建结果
     */
    @MethodLog
    @PostMapping("/create")
    public ResponseEntity<String> createTask(@RequestBody Task task) {
        log.info("Start creating a task...");
        log.info("Task title: {}", task.getTitle());
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
    @MethodLog
    @PostMapping("/taskListByCondition")
    public ResponseEntity<PageResult<Task>> getTaskListByCondition(@RequestBody Task condition, @RequestParam int pageNum, @RequestParam int pageSize) {
        log.info("Conditional query task...");
        condition.setUserId(playerStatusService.getCurrentPlayerStatus().getUserId());
        PageResult<Task> result = taskService.getTaskListByCondition(condition, pageNum, pageSize);
        return ResponseEntity.ok(result);
    }

    /**
     * 普通玩家用于编辑任务的接口
     * @param task 传入更新后的任务
     * @return
     */
    @MethodLog
    @PutMapping("/update")
    public ResponseEntity<InfoResponse> updateTask(@RequestBody Task task) {
        log.info("Start updating task...");
        task.setUserId(playerStatusService.getCurrentPlayerStatus().getUserId());
        return ResponseEntity.ok(taskService.updateTask(task));
    }

    /**
     * 根据任务 id 删除任务
     * @param id 任务 id
     * @return 删除结果
     */
    @MethodLog
    @DeleteMapping("/delete")
    public ResponseEntity<InfoResponse> deleteById(@RequestParam Long id) {
        log.info("Start deleting task...");
        log.info("deleting task id: {}", id);
        return ResponseEntity.ok(taskService.deleteById(id));
    }
}
