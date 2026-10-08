package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Employee;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sky.vo.DishItemVO;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {


    @Autowired
    SetmealMapper setmealMapper;

    @Autowired
    SetmealDishMapper setmealDishMapper;

    @Autowired
    DishMapper dishMapper;
    @Transactional
    @Override
    /**
     * 添加套餐
     */
    public void addSetmeal(SetmealDTO setmealDTO){
        Setmeal setmeal= new Setmeal();
        //对象复制
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        Long id = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish sd : setmealDishes) {
            sd.setSetmealId(id);
        }
        setmealMapper.insertSetmealDishs(setmealDishes);
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id){
        Setmeal setmeal = setmealMapper.getById(id);
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    /**
     * 分页查询
     */
    public PageResult list(SetmealPageQueryDTO setmealPageQueryDTO){
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page=setmealMapper.list(setmealPageQueryDTO);
        List<Setmeal> records=page.getResult();
        return new PageResult(page.getTotal(),records);
    }

    /**
     * 改变套餐状态
     * 业务规则：
     * - 可以对状态为起售的套餐进行停售操作，可以对状态为停售的套餐进行起售操作
     * - 起售的套餐可以展示在用户端，停售的套餐不能展示在用户端
     * - 起售套餐时，如果套餐内包含停售的菜品，则不能起售
     * @param status
     * @param id
     */
    @Transactional
    @Override
    public void changeStatus(String status,Long id){
        List<SetmealDish> setmealDishes=setmealDishMapper.getBySetmealId(id);
        for(SetmealDish setmealDish :setmealDishes){
            Long dishId =setmealDish.getDishId();
            if(dishMapper.getById(dishId).getStatus()!=1){
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        setmealMapper.updateStatus(status,id);
    }

    @Transactional
    @Override
    public void deleteSetmeal(List<Long> ids){
        List<Setmeal> setmeals = setmealMapper.getByIds(ids);
        //判断是否起售，否则无法删除
        for(Setmeal setmeal:setmeals){
            int status=setmeal.getStatus();
            if(status!=0){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //删除套餐关联表
        setmealDishMapper.deleteById(ids);
        //删除套餐表
        setmealMapper.deleteByids(ids);
    }

    /**
     * 修改菜品
     * @param setmealDTO
     * @return
     */
    @Transactional
    @Override
    public void updateSetmeal(SetmealDTO setmealDTO){
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.updateSetmeal(setmeal);
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        Long id=setmealDTO.getId();
        if(setmealDishes.size()>0&&setmealDishes!=null){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(id);
            });
        }
        setmealDishMapper.insert(setmealDishes);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
