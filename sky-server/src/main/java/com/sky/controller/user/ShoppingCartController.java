package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端购物车接口")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result addShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("购物车添加{}",shoppingCartDTO);
        shoppingCartService.addCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查询购物车数据
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list(){
        log.info("遍历购物车");
        List<ShoppingCart> shoppingCart=shoppingCartService.list();
        return Result.success(shoppingCart);
    }

    @PostMapping("/sub")
    @ApiOperation("删除单个数据")
    public Result deleteById(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除单个数据{}",shoppingCartDTO);
        shoppingCartService.deleteById(shoppingCartDTO);
        return Result.success(shoppingCartDTO);
    }


    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车所有数据")
    public Result deleteAll(){
        log.info("删购物车所有数据");
        shoppingCartService.deleteAll();
        return Result.success();
    }
}
