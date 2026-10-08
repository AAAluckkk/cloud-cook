package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 批量查询套餐
     * @param ids
     * @return
     */
    List<Setmeal> getByIds(List<Long> ids);

    @AutoFill(value= OperationType.INSERT)
    void insert(Setmeal setmeal);

    void insertSetmealDishs(List<SetmealDish> setmealDishes);

    @Select("select s.*,c.name from setmeal s left outer join category c on s.category_id=c.id where s.id=#{id}")
    Setmeal getById(Long id);

    Page<Setmeal> list(SetmealPageQueryDTO setmealPageQueryDTO);

    @Update("update setmeal set status=#{status} where id=#{id}")
    void updateStatus(String status,Long id);

    void deleteByids(List<Long> ids);

    @Select("select setmeal_dish.setmeal_id from setmeal_dish where name=#{name}")
    Long getByName(String name);

    @AutoFill(value=OperationType.UPDATE)
    void updateSetmeal(Setmeal setmeal);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

}
