package com.jacolp.task_system;

import com.jacolp.task_system.entity.PlayerStatus;
import com.jacolp.task_system.entity.Task;
import com.jacolp.task_system.mapper.PlayerStatusMapper;
import com.jacolp.task_system.mapper.TaskMapper;
import com.jacolp.task_system.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class TaskSystemApplicationTests {

//    @Autowired
//    private TaskMapper taskMapper;
//
//    @Autowired
//    private PlayerStatusMapper playerStatusMapper;
//
//    @Test
//    void goBack_1() {
//        // 1. 复原任务状态
//        Task task = taskMapper.selectById(1L);
//        task.setStatus(0);
//
//        // 2. 复原玩家状态
//        PlayerStatus status = playerStatusMapper.selectById(1L);
//        status.setCurrentExp(0L);
//        status.setTotalExp(10L);
//        status.setLevel(1);
//        status.setSpirit(1);
//        status.setBody(1);
//
//        // 3. 更新数据库
//        taskMapper.updateTaskStatus(task);
//        playerStatusMapper.updateStatus(status);
//
//        // 4. 控制台展示
//        System.out.println("数据已还原！");
//    }
//
//    @Test
//    void deleteAllData() {
//
//    }


//    // 学习 Spring 原理所使用的测试内容
//    @Autowired
//    private ApplicationContext applicationContext;  // 获取 IOC 容器
//
//    @Test
//    public void testScope() {
//        for (int i = 0; i < 100; i++) {
//            /* 不知道为什么第一个字母要用小写 */
//            Object taskController = applicationContext.getBean("taskController");
//            System.out.println(taskController); // 会拿到一千个都是一样的
//        }
//    }




}
