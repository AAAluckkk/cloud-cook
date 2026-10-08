package com.sky.service.impl;

import com.fasterxml.jackson.databind.ser.std.ByteArraySerializer;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartImpl implements ShoppingCartService {


    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Autowired
    DishMapper dishMapper;
    @Autowired
    SetmealMapper setmealMapper;
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addCart(ShoppingCartDTO shoppingCartDTO){
        //判断当前要加入的菜品是否已经添加
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);

        //已经添加就再加 1
        if(shoppingCarts!=null&& shoppingCarts.size()>0){
            ShoppingCart shoppingCart1 = shoppingCarts.get(0);
            Integer number = shoppingCart1.getNumber();
            shoppingCart1.setNumber(number+1);
            shoppingCartMapper.updateAddNumber(shoppingCart1);
        }
        //为空就直接添加
        else{
            //分为套餐和单个菜品添加
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            if(dishId!=null &&dishId>0){
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
            }else if(setmealId != null && setmealId > 0){
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);

        }
    }

    @Override
    public List<ShoppingCart> list(){
        List<ShoppingCart> shoppingCart = shoppingCartMapper.listAll();
        return shoppingCart;
    }

    /**
     * 删除单个购物车内数据
     * @param shoppingCartDTO
     */
    @Override
    public void deleteById(ShoppingCartDTO shoppingCartDTO){
        //先判断是套餐还是菜品
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.listAll();
        if(dishId!=null){
            for(ShoppingCart shoppingCart:shoppingCarts){
                if(shoppingCart.getDishId()==dishId){
                    if(shoppingCart.getNumber()>1){
                        //如果数量大于一就删除数目
                        shoppingCart.setNumber(shoppingCart.getNumber()-1);
                        shoppingCartMapper.update(shoppingCart);
                    }else {
                        //如果数量等于一就删除全部数据
                        shoppingCartMapper.delete(shoppingCart.getId());
                    }
                    break;
                }
            }
        }else{
            for(ShoppingCart shoppingCart:shoppingCarts){
                if(shoppingCart.getSetmealId()==setmealId){
                    if(shoppingCart.getNumber()>1){
                        //如果数量大于一就删除数目
                        shoppingCart.setNumber(shoppingCart.getNumber()-1);
                        shoppingCartMapper.update(shoppingCart);
                    }else {
                        //如果数量等于一就删除全部数据
                        shoppingCartMapper.delete(shoppingCart.getId());
                    }
                    break;
                }
            }
        }
    }

    /**
     * 清除购物车
     */
    @Override
    public void deleteAll(){
        shoppingCartMapper.deleteAll();
    }
}
