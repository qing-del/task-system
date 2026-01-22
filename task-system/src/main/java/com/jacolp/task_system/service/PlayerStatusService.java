package com.jacolp.task_system.service;

import com.jacolp.task_system.entity.PlayerStatus;
import org.springframework.stereotype.Service;

@Service
public interface PlayerStatusService {
    static final int EXP_INCREASE_Factor = 5;   // 经验值增加因子

    PlayerStatus getCurrentPlayerStatus();

    PlayerStatus selectByUserId(Long userId);

    /**
     * 检查等级提升
     * @param status 玩家状态表
     */
    static void checkLevelUp(PlayerStatus status) {
        if(status.getCurrentExp() >= status.getTotalExp()) {
            status.setLevel(status.getLevel() + 1);
            status.setCurrentExp(status.getCurrentExp() - status.getTotalExp());
            status.setTotalExp(status.getTotalExp() * EXP_INCREASE_Factor);
        }
    }
}
