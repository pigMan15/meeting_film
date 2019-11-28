package com.stylefeng.guns.rest.modular.order;


import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.order.OrderServiceAPI;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/order/")
public class OrderController {

    private static final String  imgPre = "http://img.meetingshop.cn/";

    @Reference(interfaceClass = OrderServiceAPI.class)
    private OrderServiceAPI orderServiceAPI;

    @RequestMapping(value = "buyTickets",method = RequestMethod.POST)
    public ResponseVO buyTickets(Integer fieldId, String soldSeats, String seatsName){

        try{

            //验证售出的票是否为真
            boolean isTrue =  orderServiceAPI.isTrueSeats(fieldId+"",soldSeats);

            //验证购买的座位是否已被销售
            boolean isNotSold = orderServiceAPI.isNotSoldSeats(fieldId+"",soldSeats);

            if(isTrue && isNotSold){
                String userId = CurrentUser.getCurrentUser();
                if(userId == null || userId.trim().length() == 0){
                    return ResponseVO.serviceFail("用户未登录");
                }
                OrderVO orderVO = orderServiceAPI.saveOrderInfo(fieldId,soldSeats,seatsName,Integer.parseInt(userId));
                if(orderVO == null){
                    log.error("购票未成功");
                    return ResponseVO.serviceFail("购票业务异常");
                }else{
                    return ResponseVO.success(orderVO);
                }
            }else{
                return ResponseVO.serviceFail("购票业务异常");
            }

            //创建订单
        }catch(Exception e){
            log.error("购票业务异常",e);
            return ResponseVO.serviceFail("购票业务异常");
        }

    }

    @RequestMapping(value = "getOrderInfo" ,method=RequestMethod.POST)
    public ResponseVO getOrderInfo(
            @RequestParam(name = "nowPage", required = false, defaultValue = "1") Integer nowPage,
            @RequestParam(name = "pageSize", required = false, defaultValue = "5") Integer pageSize
    ){

        //获取当前登录用户信息
        String userId = CurrentUser.getCurrentUser();


        //获取该用户的所有订单
        Page<OrderVO> page = new Page<>(nowPage,pageSize);
        if(userId != null && userId.trim().length() > 0){
            Page<OrderVO> result = orderServiceAPI.getOrderByUserId(Integer.valueOf(userId),page);
            return ResponseVO.success(imgPre,nowPage,(int)result.getPages(),result.getRecords());
        }else{
            return ResponseVO.serviceFail("用户未登录");
        }

    }

}