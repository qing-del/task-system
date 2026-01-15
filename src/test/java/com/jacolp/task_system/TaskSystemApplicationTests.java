package com.jacolp.task_system;

import com.jacolp.task_system.entity.PlayerStatus;
import com.jacolp.task_system.entity.Task;
import com.jacolp.task_system.mapper.PlayerStatusMapper;
import com.jacolp.task_system.mapper.TaskMapper;
import com.jacolp.task_system.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TaskSystemApplicationTests {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private PlayerStatusMapper playerStatusMapper;

    @Test
    void goBack_1() {
        // 1. 复原任务状态
        Task task = taskMapper.selectById(1L);
        task.setStatus(0);

        // 2. 复原玩家状态
        PlayerStatus status = playerStatusMapper.selectById(1L);
        status.setCurrentExp(0);
        status.setTotalExp(10);
        status.setLevel(1);
        status.setSpirit(1);
        status.setBody(1);

        // 3. 更新数据库
        taskMapper.updateTaskStatus(task);
        playerStatusMapper.updateStatus(status);

        // 4. 控制台展示
        System.out.println("数据已还原！");
    }

    @Test
    void deleteAllData() {

    }
}
