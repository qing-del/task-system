package com.jacolp.task_system.mapper;

import com.jacolp.task_system.entity.Item;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShopMapper {
    /**
     * 插入物品
     * @param item 需要插入的物品
     * @return 如果不为0即为插入成功
     */
    @Insert("insert into shop_items (name, description, cost_type, cost_value," +
            " effect_type, effect_value, stack)  value (#{name}, #{description}, #{costType}" +
            "#{costValue}, #{effectType}, #{effectValue}, #{stack})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Item item);

    /**
     * 根据id查询物品
     * @param id 需要查询的id
     * @return 插入的玩家数据
     */
    @Select("select * from shop_items where id = #{id}")
    Item selectById(long id);

    /**
     * 查询所有物品（条件查询）
     * @return 所有物品数据
     */
    List<Item> selectAll(Item condition);

    @Update("update shop_items set name=#{name}, description=#{description}, cost_type=#{costType}, " +
            "cost_value=#{costValue}, effect_type=#{effectType}, effect_value=#{effectValue}, " +
            "stack=#{stack} where id=#{id}")
    int update(Item item);
}
