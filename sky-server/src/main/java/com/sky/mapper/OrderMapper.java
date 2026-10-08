package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.sky.dto.GoodsSalesDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from order_detail where order_id=#{id}")
    OrderDetail getOderDetailById(Long id);

    @Update("update orders set status=#{cancelled} where id =#{id}")
    void updateStatus(Integer cancelled,Long id);

    @Select("select * from orders")
    List<Orders> list();

    @Select("select * from orders where status=#{status} and order_time<#{time}")
    List<Orders> getByStatusAndOrderTimeOut(Integer status, LocalDateTime time);

    /**
     * 根据参数动态查询营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据参数动态统计订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 销量排名top10
     * @param map
     * @return
     */
    List<GoodsSalesDTO> getTop10(Map map);
}
