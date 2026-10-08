package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    @AutoFill(value=OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> list(DishPageQueryDTO dishPageQueryDTO);

    List<Dish> listByDish(Dish dish);

    @Select("select * from dish where id=#{id}")
    Dish getById(Long id);


    List<Dish> getByIds(List<Long> ids);

    void deleteByIds(List<Long> ids);

    @AutoFill(value = OperationType.UPDATE)
    void updateById(Dish dish);

    @Select("select * from dish where category_id=#{categoryId}")
    List<Dish> listById(Integer categoryId);

    @Update("update dish set status=#{status} where id=#{id}")
    void changeStauts(String status,Long id);
}
