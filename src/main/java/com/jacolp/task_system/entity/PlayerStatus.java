package com.jacolp.task_system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatus {
    private Long id;
    private Integer spirit;
    private Integer body;
    private Long currentExp;
    private Long totalExp;
    private Integer level;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    Date updateTime;

    private Long userId; // 跟随 Jwt 出现的 UserId

    private Long coin;  // 加入金币
}
