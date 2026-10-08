package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderMapper orderMapper;
    @Autowired
    AddressBookMapper addressBookMapper;
    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    WebSocketServer webSocketServer;

    /**
     * 用户下单
     * @param orderSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO orderSubmitDTO){
        //解决业务异常，购物车为空，地址为空
        AddressBook addressBook = addressBookMapper.getById(orderSubmitDTO.getAddressBookId());
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId= BaseContext.getCurrentId();
        ShoppingCart shoppingCart=new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList=shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList.size()==0||shoppingCartList==null){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //订单表插入一条数据
        Orders order=new Orders();
        BeanUtils.copyProperties(orderSubmitDTO,order);
        order=setOrder(order);
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        orderMapper.insert(order);

        //订单明细表插入多条数据
        List<OrderDetail> orderDetails=new ArrayList<>();
        for(ShoppingCart sp:shoppingCartList){
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(sp,orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        //清空当前用户购物车
        shoppingCartMapper.deleteByUserId(userId);
        //WebSocket向管理端推送来单提醒
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1); // 1=来单提醒
        map.put("orderId", order.getId());
        map.put("content", "您有新的订单，订单号：" + order.getNumber());
        String message = com.alibaba.fastjson.JSON.toJSONString(map);
        webSocketServer.sendToAllClient(message);
        //封装VO返回结果
        OrderSubmitVO ordersSubmitVO=OrderSubmitVO.builder()
                .id(order.getId())
                .orderTime(order.getOrderTime())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .build();
        return ordersSubmitVO;
    }

    /**
     * 分页查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO){
        // 只查询当前用户的订单
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        log.info("分页参数: page={}, pageSize={}, status={}, userId={}",
                ordersPageQueryDTO.getPage(),
                ordersPageQueryDTO.getPageSize(),
                ordersPageQueryDTO.getStatus(),
                ordersPageQueryDTO.getUserId());
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page=orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list=page.getResult();
        //将每个orders的id提取出来，查询OrderDetail
        List<Long> ids=new ArrayList<>();
        for(Orders orders:list){
            Long id = orders.getId();
            ids.add(id);
        }
        log.info("list.size={}, ids={}", list.size(), ids);
        if (!ids.isEmpty()) {
            List<OrderDetail> orderDetails = orderDetailMapper.selectBatch(ids);
            // 按orderId分组
            Map<Long, List<OrderDetail>> detailMap = new HashMap<>();
            for (OrderDetail detail : orderDetails) {
                Long orderId = detail.getOrderId();
                if (!detailMap.containsKey(orderId)) {
                    detailMap.put(orderId, new ArrayList<>());
                }
                detailMap.get(orderId).add(detail);
            }
            // 塞回每个OrderVO，没有详情的给空List而非null
            for (OrderVO orderVO : list) {
                List<OrderDetail> details = detailMap.get(orderVO.getId());
                orderVO.setOrderDetailList(details != null ? details : new ArrayList<>());
            }
        } else {
            // 没有订单时，给每个OrderVO设空List
            for (OrderVO orderVO : list) {
                orderVO.setOrderDetailList(new ArrayList<>());
            }
        }
        return new PageResult(page.getTotal(), list);
    }

    private Orders setOrder(Orders order) {
        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(Orders.UN_PAID);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(BaseContext.getCurrentId());
        return order;
    }

    /**
     * 查询订单详情
     * @param id 订单id
     * @return
     */
    @Override
    public OrderVO getOrderDetailById(Long id){
        // 1. 查订单主表
        Orders orders = orderMapper.getById(id);
        // 2. 查订单明细
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        List<OrderDetail> orderDetails = orderDetailMapper.selectBatch(ids);
        // 3. 组装 OrderVO
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails != null ? orderDetails : new ArrayList<>());
        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void updateStatus(Long id){
        orderMapper.updateStatus(Orders.CANCELLED,id);
    }

    /**
     * 订单支付（跳过真实微信支付，直接修改订单状态）
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 直接修改订单状态：待付款 → 待接单
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return new OrderPaymentVO();

    }

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    @Override
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void oneMore(Long id){
        List<OrderDetail> list =orderDetailMapper.selectByOrdersId(id);
        List<ShoppingCart> shoppingCarts=new ArrayList<>();
        for(OrderDetail orderDetail:list){
            ShoppingCart shoppingCart=new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCarts.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(shoppingCarts);
    }

    /**
     * 取消接单
     * @param ordersCancelDTO
     */
    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO){
        orderMapper.update(cancelM(ordersCancelDTO));
    }

    private Orders cancelM(OrdersCancelDTO ordersCancelDTO){
        Orders orders=new Orders();
        orders.setUserId(BaseContext.getCurrentId());
        orders.setStatus(Orders.CANCELLED);
        orders.setId(ordersCancelDTO.getId());
        orders.setCancelReason(orders.getCancelReason());
        orders.setOrderTime(LocalDateTime.now());
        return orders;
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getById(Long id){
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetails=orderDetailMapper.selectByOrdersId(id);
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 搜索订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult list(OrdersPageQueryDTO ordersPageQueryDTO){
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page =orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list=page.getResult();
        return new PageResult(page.getTotal(),list);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO getOrderStatisitcs(){
        //2待接单 3已接单 4派送中
        List<Orders> orders = orderMapper.list();
        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(0);
        orderStatisticsVO.setConfirmed(0);
        orderStatisticsVO.setDeliveryInProgress(0);
        for(Orders od:orders){
            switch (od.getStatus()){
                case 2:orderStatisticsVO.setToBeConfirmed(orderStatisticsVO.getToBeConfirmed()+1);break;
                case 3:orderStatisticsVO.setConfirmed(orderStatisticsVO.getConfirmed()+1);break;
                case 4:orderStatisticsVO.setDeliveryInProgress(orderStatisticsVO.getDeliveryInProgress()+1);break;
            }
        }
        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void accept(OrdersConfirmDTO ordersConfirmDTO){
        Orders orders=Orders.builder()
                        .id(ordersConfirmDTO.getId())
                        .status(Orders.CONFIRMED)
                                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersCancelDTO
     */
    @Override
    public void refuse(OrdersCancelDTO ordersCancelDTO){
        Orders orders=Orders.builder()
                .id(ordersCancelDTO.getId())
                .rejectionReason(ordersCancelDTO.getCancelReason())
                .status(Orders.CANCELLED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 派送
     */
    @Override
    public void delivery(Long id){
        Orders orders=Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    public void complete(Long id){
        Orders orders=Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 用户催单
     * @param id
     */
    public void reminder(Long id){
        Orders orders = orderMapper.getById(id);
        if(orders==null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map map=new HashMap<>();
        map.put("type",2);//2是用户催单
        map.put("orderId",id);
        map.put("content","订单号"+orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));

    }
}
