package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单细节
     * @param orderDetails
     */
    void insertBatch(List<OrderDetail> orderDetails);

    /**
     * 批量查询
     * @param ids
     * @return
     */
    List<OrderDetail> selectBatch(List<Long> ids);

    /**
     * 根据orders id 查询
     * @param id
     * @return
     */
    List<OrderDetail> selectByOrdersId(Long id);
}
