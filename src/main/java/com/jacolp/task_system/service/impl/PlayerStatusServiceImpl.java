package com.jacolp.task_system.service.impl;

import com.jacolp.task_system.entity.PlayerStatus;
import com.jacolp.task_system.entity.User;
import com.jacolp.task_system.exception.AuthRuntimeException;
import com.jacolp.task_system.mapper.PlayerStatusMapper;
import com.jacolp.task_system.mapper.UserMapper;
import com.jacolp.task_system.service.PlayerStatusService;
import com.jacolp.task_system.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerStatusServiceImpl implements PlayerStatusService {
    static final int INITIAL_BODY_VALUE = 1;    // 初始体魄值
    static final int INITIAL_SPIRIT_VALUE = 1;  // 初始精神值
    static final long INITIAL_EXP_VALUE = 0;     // 初始经验值
    static final long INITIAL_TOTAL_EXP_VALUE = 10;   // 初始所需经验值
    static final int INITIAL_LEVEL_VALUE = 1;   // 初始经验等级
    static final long INITIAL_COIN_VALUE = 0;    // 初始金币数量

    @Autowired private UserMapper userMapper;
    @Autowired private PlayerStatusMapper playerStatusMapper;

    @Override
    public PlayerStatus getCurrentPlayerStatus() {
        // 1. 先获取现在玩家的用户名
        String username = SecurityUtil.getCurrentUsername();

        // 2. 通过玩家用户名来获取玩家的用户表
        User user = userMapper.findByUsername(username);
        if (user == null) throw new AuthRuntimeException("用户状态异常！");

        // 3. 通过用户表来查询用户的玩家状态表
        PlayerStatus status = playerStatusMapper.selectByUserId(user.getId());
        if (status == null) {
            // 为新玩家创建一个玩家状态表
            status = new PlayerStatus();

            // 设定一些基础数值
            status.setSpirit(INITIAL_SPIRIT_VALUE);
            status.setBody(INITIAL_BODY_VALUE);
            status.setCurrentExp(INITIAL_EXP_VALUE);
            status.setTotalExp(INITIAL_TOTAL_EXP_VALUE);
            status.setLevel(INITIAL_LEVEL_VALUE);
            status.setCoin(INITIAL_COIN_VALUE);
            status.setUserId(user.getId());
            playerStatusMapper.registerInsert(status);

            status = playerStatusMapper.selectByUserId(user.getId());
        }
        return status;
    }

    @Override
    public PlayerStatus selectByUserId(Long userId) {
        return playerStatusMapper.selectByUserId(userId);
    }
}
