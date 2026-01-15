package com.jacolp.task_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.DriverManager;

@SpringBootApplication
public class TaskSystemApplication {
    public static void main(String[] args) {
        // 添加数据库连接测试
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/task-system",
                "root",
                "121235")) {
            System.out.println("✅ 数据库连接成功!");
        } catch (Exception e) {
            System.out.println("❌ 数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }

        SpringApplication.run(TaskSystemApplication.class, args);
    }
}