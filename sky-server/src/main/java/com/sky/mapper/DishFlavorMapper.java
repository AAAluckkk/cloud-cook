package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据id 批量删除口味
     * @param ids
     */
    void deleteByIds(List<Long> ids);


    @Delete("delete from dish_flavor where dish_id=#{dishId}")
    void deleteById(Long dishId);

    @Select("select * from dish_flavor where dish_id =#{id}")
    List<DishFlavor> getById(Long id);

    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
