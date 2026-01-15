package com.jacolp.task_system.service;

import com.jacolp.task_system.dto.PageResult;
import com.jacolp.task_system.dto.InfoResponse;
import com.jacolp.task_system.entity.Item;
import org.springframework.stereotype.Service;

@Service
public interface ShopService {
    /*
    1. 查询列表
    2. 查询单个
    3. 更新库存
    4. 购买接口
     */

    /**
     * 获取商品列表（分页查询）
     * @return 获取到的商品列表
     */
    PageResult<Item> list(Item condition, int pageNum, int pageSize);

    /**
     * 根据 ID 查询商品
     * @param id 商品 ID
     * @return 查询到的商品
     */
    Item selectById(Long id);

    /**
     * 更新商品
     * @param item 商品
     * @return 是否更新成功
     */
    boolean updateItem(Item item);

    /**
     * 购买商品
     * @param itemId 商品 ID
     * @param playerId 玩家 ID
     * @return 是否购买成功
     */
    InfoResponse buyItem(Long itemId, Long playerId);
}
