package com.jacolp.task_system.controller;

import com.jacolp.task_system.dto.PageResult;
import com.jacolp.task_system.dto.InfoResponse;
import com.jacolp.task_system.entity.Item;
import com.jacolp.task_system.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/shop")
public class ShopController {
    @Autowired private ShopService shopService;

    /**
     * 获取商品列表
     * @param condition 查询条件
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 商品列表
     */
    @PostMapping("/list")
    public ResponseEntity<PageResult<Item>> list(Item condition, int pageNum, int pageSize) {
        return ResponseEntity.ok(shopService.list(condition, pageNum, pageSize));
    }

    /**
     * 根据 ID 查询商品
     * @param id 商品 ID
     * @return 商品
     */
    @GetMapping("/selectById")
    public ResponseEntity<Item> selectById(Long id) {
        return ResponseEntity.ok(shopService.selectById(id));
    }

    /**
     * 购买商品
     * @param itemId 商品 ID
     * @param playerId 玩家 ID
     * @return 是否购买成功
     */
    @PostMapping("/buyItem")
    public ResponseEntity<InfoResponse> buyItem(Long itemId, Long playerId) {
        return ResponseEntity.ok(shopService.buyItem(itemId, playerId));
    }
}
