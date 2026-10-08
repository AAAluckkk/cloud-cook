package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时不支付订单
     */
    @Scheduled(cron="0 * * * * ? ")
    public void processTimeOutOrder(){
        log.info("查看超时订单{}", LocalDateTime.now());
        LocalDateTime time=LocalDateTime.now().plusMinutes(-15);//当前时间-15
        List<Orders> orders= orderMapper.getByStatusAndOrderTimeOut(Orders.PENDING_PAYMENT,time);
        if(orders.size()>0&&orders!=null){
            for (Orders order:orders){
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }

    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron="0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("处理在派送的订单{}",LocalDateTime.now());
        LocalDateTime time=LocalDateTime.now().plusMinutes(-60);
        List<Orders> orders= orderMapper.getByStatusAndOrderTimeOut(Orders.PENDING_PAYMENT,time);
        if(orders.size()>0&&orders!=null){
            for (Orders order:orders){
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
