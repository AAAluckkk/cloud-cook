package com.sky.controller.user;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags="店铺状态设置")
@Slf4j
public class ShopController {

    private static final String KEY="STATUS_SHOP";
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取状态")
    public Result getShopStatus(){
        Integer status=(Integer)redisTemplate.opsForValue().get(KEY);
        log.info("店铺状态为,{}",status==1?"在线":"下线");
        return Result.success(status);
    }
}
