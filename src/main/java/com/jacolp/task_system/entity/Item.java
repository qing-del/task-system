package com.jacolp.task_system.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private Long id;
    private String name;
    private String description;
    private Integer costType;   // 0：经验，1：精神，2：体魄，3：金币
    private Long costValue;
    private Integer effectType; // 0：经验，1：精神，2：体魄
    private Long effectValue;
    private Long stack; // -1 表示无穷
}
