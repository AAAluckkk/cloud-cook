package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 保存口味
     * @param dishDTO
     */
    @Transactional
    @Override
    public void saveWithFlavors(DishDTO dishDTO){
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        Long dishId=dish.getId();
        List<DishFlavor> dishFlavors=dishDTO.getFlavors();
        if(dishFlavors!=null&&dishFlavors.size()>0){
            dishFlavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(dishFlavors);
        }

    }

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO){
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.list(dishPageQueryDTO);
        List<DishVO> result=page.getResult();
        return new PageResult(page.getTotal(),result);
    }

    /**
     * 删除菜品
     * @param ids
     */
    @Transactional
    @Override
    public void deleteByIds(List<Long> ids){
        //判断是否存在起售的菜品
        List<Dish> dishes=dishMapper.getByIds(ids);
        for(Dish dish: dishes){
            if(dish.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //关联的套餐
        List<Long> setmealId= setmealDishMapper.getSetmealIdByDishId(ids);

        if(setmealId!=null && setmealId.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        dishMapper.deleteByIds(ids);
        //删除菜品
        dishFlavorMapper.deleteByIds(ids);
        //关联的口味删除
    }

    /**
     * 根据id查询口味和菜品
     * @param id
     * @return
     */
    @Transactional
    @Override
    public DishDTO getDishWithFlavorById(Long id){
        Dish dish= dishMapper.getById(id);
        List<DishFlavor> dishFlavors= dishFlavorMapper.getById(id);
        DishDTO dishDTO=new DishDTO();
        BeanUtils.copyProperties(dish,dishDTO);
        dishDTO.setFlavors(dishFlavors);
        return dishDTO;
    }

    /**
     * 根据id修改菜品和口味
     * @param dishVO
     */
    @Transactional
    @Override
    public void updateWithFlavor(DishVO dishVO){
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishVO,dish);
        dishMapper.updateById(dish);
        Long dishId=dish.getId();
        dishFlavorMapper.deleteById(dishId);
        List<DishFlavor> dishFlavors=dishVO.getFlavors();
        if(dishFlavors!=null&&dishFlavors.size()>0){
            dishFlavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(dishFlavors);
        }
    }
    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> listById(Integer categoryId){
        List<Dish> dishes=dishMapper.listById(categoryId);
        return dishes;
    }

    @Override
    public void changeStatus(String status,Long id){
        dishMapper.changeStauts(status,id);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.listByDish(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
