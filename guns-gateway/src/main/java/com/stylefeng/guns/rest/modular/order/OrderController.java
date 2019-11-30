package com.stylefeng.guns.rest.modular.order;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.baomidou.mybatisplus.plugins.Page;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.stylefeng.guns.api.alipay.AlipayAPI;
import com.stylefeng.guns.api.alipay.vo.AlipayInfoVO;
import com.stylefeng.guns.api.alipay.vo.AlipayResultVO;
import com.stylefeng.guns.api.order.OrderServiceAPI;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.core.util.TokenBucketUtil;
import com.stylefeng.guns.core.util.ToolUtil;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/order/")
public class OrderController {

    private static TokenBucketUtil tokenBucketUtil = new TokenBucketUtil();

    private static final String  imgPre = "http://img.meetingshop.cn/";



    @Reference(
            interfaceClass = OrderServiceAPI.class,
            check = false,
            group = "order2019"
    )
    private OrderServiceAPI orderServiceAPI;

    @Reference(
            interfaceClass = OrderServiceAPI.class,
            check = false,
            group = "order2018"
    )
    private OrderServiceAPI orderServiceAPI2018;

    @Reference(
            interfaceClass = OrderServiceAPI.class,
            check = false,
            group = "order2017"
    )
    private OrderServiceAPI orderServiceAPI2017;

    @Reference(interfaceClass = AlipayAPI.class,check = false)
    private AlipayAPI alipayAPI;

    private ResponseVO error(Integer fieldId, String soldSeats, String seatsName){
        return ResponseVO.serviceFail("抱歉，下单的人太多了，请稍后再重试");
    }

    @HystrixCommand(fallbackMethod = "error", commandProperties = {
            @HystrixProperty(name="execution.isolation.strategy", value = "THREAD"),
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value
                    = "4000"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50")
                    }, threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "1"),
                    @HystrixProperty(name = "maxQueueSize", value = "10"),
                    @HystrixProperty(name = "keepAliveTimeMinutes", value = "1000"),
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "8"),
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "12"),
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "1500")
            })
    @RequestMapping(value = "buyTickets",method = RequestMethod.POST)
    public ResponseVO buyTickets(Integer fieldId, String soldSeats, String seatsName){

        try{
            System.out.println(tokenBucketUtil.getToken());
            if(tokenBucketUtil.getToken()) {
                System.out.println("test");
                //验证售出的票是否为真
                boolean isTrue = orderServiceAPI.isTrueSeats(fieldId + "", soldSeats);

                //验证购买的座位是否已被销售
                boolean isNotSold = orderServiceAPI.isNotSoldSeats(fieldId + "", soldSeats);

                if (isTrue && isNotSold) {
                    String userId = CurrentUser.getCurrentUser();
                    if (userId == null || userId.trim().length() == 0) {
                        return ResponseVO.serviceFail("用户未登录");
                    }
                    OrderVO orderVO = orderServiceAPI.saveOrderInfo(fieldId, soldSeats, seatsName, Integer.parseInt(userId));
                    if (orderVO == null) {
                        log.error("购票未成功");
                        return ResponseVO.serviceFail("购票业务异常");
                    } else {
                        return ResponseVO.success(orderVO);
                    }
                } else {
                    return ResponseVO.serviceFail("购票业务异常");
                }
            }else{
                log.error("购票未成功");
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

            Page<OrderVO> result2017 = orderServiceAPI2017.getOrderByUserId(Integer.valueOf(userId),page);

            Page<OrderVO> result2018 = orderServiceAPI2018.getOrderByUserId(Integer.valueOf(userId),page);

            int totalPages = (int) (result.getPages() + result2017.getPages()+ result2018.getPages());

            List<OrderVO> orderVOList = new ArrayList<>();
            orderVOList.addAll(result.getRecords());
            orderVOList.addAll(result2017.getRecords());
            orderVOList.addAll(result2018.getRecords());

            return ResponseVO.success(imgPre,nowPage,totalPages,orderVOList);
        }else{
            return ResponseVO.serviceFail("用户未登录");
        }

    }


    @RequestMapping(value = "getPayInfo",method = RequestMethod.POST)
    public ResponseVO getPayInfo(@RequestParam("orderId") String orderId){

        String userId = CurrentUser.getCurrentUser();
        if(userId == null || userId.trim().length() == 0){
            return ResponseVO.serviceFail("用户未登录");
        }

        AlipayInfoVO alipayInfoVO = alipayAPI.getQRCode(orderId);


        return ResponseVO.success(imgPre,alipayInfoVO);
    }


    @RequestMapping(value = "getPayResult",method = RequestMethod.POST)
    public ResponseVO getPayResult(
            @RequestParam("orderId") String orderId,
                                   @RequestParam(name="tryNums",required = false,defaultValue = "1") Integer tryNums){

        String userId = CurrentUser.getCurrentUser();
        if(userId == null || userId.trim().length() == 0){
            return ResponseVO.serviceFail("用户未登录");
        }

        //将当前登录人的信息传递给后端
        RpcContext.getContext().setAttachment("userId",userId);



        //判断支付是否超时

        if(tryNums >= 4){
            return  ResponseVO.serviceFail("订单支付失败，请稍后");
        }else{
            AlipayResultVO alipayResultVO = alipayAPI.getOrderStatus(orderId);

            if(alipayResultVO == null || ToolUtil.isEmpty(alipayResultVO.getOrderId())){
                AlipayResultVO serviceFialVO = new AlipayResultVO();
                serviceFialVO.setOrderId(orderId);
                serviceFialVO.setOrderStatus(0);
                serviceFialVO.setOrderMsg("支付不成功");
                return ResponseVO.success(serviceFialVO);
            }

            return ResponseVO.success(alipayResultVO);
        }


    }

}
