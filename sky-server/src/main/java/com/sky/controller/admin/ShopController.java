package com.sky.controller.admin;


import com.sky.dto.OrdersCancelDTO;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags="店铺状态设置")
@Slf4j
public class ShopController {

    private static final String KEY="STATUS_SHOP";
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 更新店铺状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置状态")
    public Result setShopStatus(@PathVariable Integer status){
        log.info("更改状态为,{}",status==1?"在线":"下线");
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取状态")
    public Result getShopStatus(){
        Integer status=(Integer)redisTemplate.opsForValue().get(KEY);
        log.info("店铺状态为,{}",status==1?"在线":"下线");
        return Result.success(status);
    }

}
