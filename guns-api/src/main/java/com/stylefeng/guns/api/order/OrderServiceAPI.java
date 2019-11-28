package com.stylefeng.guns.api.order;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.order.vo.OrderVO;

import java.util.List;

public interface OrderServiceAPI {

    //验证售出的票是否为真
    boolean isTrueSeats(String fieldId,String seats);


    //验证购买的座位是否已被销售
    boolean isNotSoldSeats(String fieldId,String seats);

    //创建订单
    OrderVO saveOrderInfo(Integer fieldId, String soldSeats, String seatsName,Integer userId);

    //获取当前登录用户信息
    Page<OrderVO> getOrderByUserId(Integer userId,Page<OrderVO> page);

    //获取该用户的所有订单
    String getSoldSeatsByFieldId(Integer fieldId);
}
