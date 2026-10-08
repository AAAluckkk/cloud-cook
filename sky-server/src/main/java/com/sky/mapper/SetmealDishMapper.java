package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id批量查询套餐id
     * @param ids
     * @return
     */
    List<Long> getSetmealIdByDishId(List<Long> ids);

    void deleteById(List<Long> ids);

    /**
     * 根据套餐id查询setmeal_dish
     * @param id
     */
    @Select("select * from setmeal_dish where setmeal_id =#{id}")
    List<SetmealDish> getBySetmealId(Long id);

    void insert(List<SetmealDish> setmealDishes);
}
