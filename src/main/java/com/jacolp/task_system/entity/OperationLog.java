package com.jacolp.task_system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {
    private Long id;
    private Long userId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    private String className;
    private String methodName;
    private String params;
    private String returnValue; // 出现异常时为 null
    private String exception; // 没有异常时为 null
    private Long costTime;  // 耗时 单位ms
}
