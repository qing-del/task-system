package com.jacolp.task_system.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jacolp.task_system.dto.PageResult;
import com.jacolp.task_system.dto.InfoResponse;
import com.jacolp.task_system.entity.Item;
import com.jacolp.task_system.entity.PlayerStatus;
import com.jacolp.task_system.exception.ItemException;
import com.jacolp.task_system.mapper.PlayerStatusMapper;
import com.jacolp.task_system.mapper.ShopMapper;
import com.jacolp.task_system.service.PlayerStatusService;
import com.jacolp.task_system.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ShopServiceImpl implements ShopService {

    @Autowired private ShopMapper shopMapper;
    @Autowired private PlayerStatusMapper playerStatusMapper;

    /**
     * 查询所有物品
     * 分页查询
     * @return 所有物品
     */
    @Override
    public PageResult<Item> list(Item condition, int pageNum, int pageSize) {
        // 1. 设置分页参数
        PageHelper.startPage(pageNum, pageSize);

        // 2. 查询所有物品
        List<Item> list = shopMapper.selectAll(condition);

        // 3. 解析并封装结果
        Page<Item> p = (Page<Item>) list;
        return new PageResult<Item>(p.getTotal(), p.getResult());
    }

    /**
     * 根据 id 查询物品
     * @param id 物品 id
     * @return 物品
     */
    @Override
    public Item selectById(Long id) {
        return shopMapper.selectById(id);
    }

    /**
     * 更新物品
     * @param item 需要更新的物品
     * @return 是否更新成功
     */
    @Override
    public boolean updateItem(Item item) {
        if (item.getId() == null || item.getId() == 0) {
            log.error("更新物品时 出现非法 id 异常");
            throw new ItemException("非法物品ID异常！");
        }
        return shopMapper.update(item) > 0;
    }

    /**
     * 购买物品
     * @param itemId 物品id
     * @param playerId 玩家id
     * @return 是否购买成功
     */
    @Override
    @Transactional
    public InfoResponse buyItem(Long itemId, Long playerId) {
        // 1. 检测有没有库存
        Item item = shopMapper.selectById(itemId);
        if (item.getStack() <= 0 && item.getStack() != -1) {
            log.warn("物品已售罄！");
            return new InfoResponse(false, "物品已售罄！");
        }

        // 2. 检测玩家余额是否充足
        PlayerStatus status = playerStatusMapper.selectById(playerId);
        // 2.1 设置玩家货币类型 和 货币的余额
        Long money;
        switch (item.getCostType()) {
            case 0:
                if (status.getTotalExp() < item.getCostValue()) {
                    log.warn("玩家等级未解锁商品！");
                    throw new ItemException("玩家等级未解锁商品！");
                }
                money = status.getCurrentExp();
                break;
            case 1:
                money = (long) status.getSpirit();
                break;
            case 2:
                money = (long) status.getBody();
                break;
            case 3 :
                money = status.getCoin();
                break;
            default:
                log.error("非法商品价格类型！");
                throw new ItemException("非法商品价格类型！");
        }

        // 2.2 检测玩家余额是否充足
        if (money < item.getCostValue()) {
            log.warn("玩家余额不足！");
            return new InfoResponse(false, "玩家余额不足！");
        }

        // 2.3 更新玩家货币值
        switch (item.getCostType()) {
            case 0:
                status.setCurrentExp(money - item.getCostValue());
                break;
            case 1:
                status.setSpirit((int)(money - item.getCostValue()));
                break;
            case 2:
                status.setBody((int)(money - item.getCostValue()));
                break;
            case 3 :
                status.setCoin(money - item.getCostValue());
                break;
            default:
                log.error("非法商品价格类型！");
                throw new ItemException("非法商品价格类型！");
        }

        // 代码能跑到这里说明购买成功了
        // 3. 更新玩家状态
        // 3.1 根据影响类型来更新玩家的状态表
        switch (item.getEffectType()) {
            case 0:
                status.setCurrentExp(status.getCurrentExp() + item.getEffectValue());
                PlayerStatusService.checkLevelUp(status);
                break;
            case 1:
                status.setSpirit((int)(status.getSpirit() + item.getEffectValue()));
                break;
            case 2:
                status.setBody((int)(status.getBody() + item.getEffectValue()));
                break;
            default:
                log.error("非法商品效果类型！");
                throw new ItemException("非法商品效果类型！");
        }

        // 3.2 更新玩家数据库
        int res1 = playerStatusMapper.updateStatus(status);
        if (res1 <= 0) {
            log.error("更新玩家状态失败！");
            throw new ItemException("更新玩家状态失败！");
        }

        // 4. 扣除库存并且更新数据库
        if (item.getStack() != -1) {
            item.setStack(item.getStack() - 1);
            int res2 = shopMapper.update(item);
            if (res2 <= 0) {
                log.error("更新商品库存失败！");
                throw new ItemException("更新商品库存失败！");
            }
        }

        return new InfoResponse(true, "购买成功！");
    }
}
