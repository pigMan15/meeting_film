package com.stylefeng.guns.rest.modular.order.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.CinemaServiceAPI;
import com.stylefeng.guns.api.cinema.vo.FilmInfoVO;
import com.stylefeng.guns.api.cinema.vo.OrderQueryVO;
import com.stylefeng.guns.api.order.OrderServiceAPI;
import com.stylefeng.guns.api.order.vo.OrderVO;
import com.stylefeng.guns.core.util.UUIDUtil;
import com.stylefeng.guns.rest.common.persistence.dao.MoocOrderTMapper;
import com.stylefeng.guns.rest.common.persistence.model.MoocOrderT;
import com.stylefeng.guns.rest.common.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.net.ftp.FtpClient;

import javax.management.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
@Service(interfaceClass = OrderServiceAPI.class, group = "default")
public class DefaultOrderServiceImpl implements OrderServiceAPI {

    @Autowired
    private MoocOrderTMapper moocOrderTMapper;

    @Autowired
    private FTPUtil ftpUtil;

    @Reference(interfaceClass = CinemaServiceAPI.class)
    private CinemaServiceAPI cinemaServiceAPI;

    @Override
    public boolean isTrueSeats(String fieldId, String seats) {
        //根据FieldId找到对应的座位位置图
        String seatPath = moocOrderTMapper.getSeatsByFieldId(fieldId);
        System.out.println(seatPath);
        //读取位置图，判断seats是否为真
        String fileStrByAddress = ftpUtil.getFileStrByAddress(seatPath);
        JSONObject jsonObject = JSONObject.parseObject(fileStrByAddress);
        String ids = jsonObject.get("ids").toString();

        String[] seatArrs = seats.split(",");
        String[] idArrs = ids.split(",");
        int count = 0;
        for(String id : idArrs){
            for(String seat : seatArrs){
                if(id.equalsIgnoreCase(seat)){
                    count++;
                }
            }
        }

        if(count == seatArrs.length){
            return true;
        }else{
            return false;
        }

    }


    //判断座位是否已售
    @Override
    public boolean isNotSoldSeats(String fieldId, String seats) {
        EntityWrapper<MoocOrderT> entityWrapper =new EntityWrapper<>();
        entityWrapper.eq("field_id",fieldId);
        List<MoocOrderT> list = moocOrderTMapper.selectList(entityWrapper);
        String[] seatArrs = seats.split(",");

        for(MoocOrderT moocOrderT : list){
            String[] ids = moocOrderT.getSeatsIds().split(",");
            for(String id : ids){
                for(String seat : seatArrs){
                    if(seat.equalsIgnoreCase(id)){
                        return false;
                    }
                }
            }
        }

        return true;
    }

    //创建新订单
    @Override
    public OrderVO saveOrderInfo(Integer fieldId, String soldSeats, String seatsName, Integer userId) {


        //编号
        String uuid = UUIDUtil.genUuid();

        //影片信息
        FilmInfoVO filmInfoVO = cinemaServiceAPI.getFilmInfoByFieldId(fieldId);
        Integer filmId = Integer.valueOf(filmInfoVO.getFilmId());

        //获取影院信息
        OrderQueryVO orderQueryVO = cinemaServiceAPI.getOrderNeeds(fieldId);
        Integer cinemaId = Integer.valueOf(orderQueryVO.getCinemaId());
        double filmPrice = Double.valueOf(orderQueryVO.getFilmPrice());

        //求订单总金额
        int solds = soldSeats.split(",").length;
        double totalPrice = getTotalPrice(solds,filmPrice);

        MoocOrderT moocOrderT = new MoocOrderT();
        moocOrderT.setUuid(uuid);
        moocOrderT.setSeatsName(seatsName);
        moocOrderT.setSeatsIds(soldSeats);
        moocOrderT.setOrderUser(userId);
        moocOrderT.setOrderPrice(totalPrice);
        moocOrderT.setFilmId(filmId);
        moocOrderT.setFieldId(fieldId);
        moocOrderT.setCinemaId(cinemaId);

        Integer count = moocOrderTMapper.insert(moocOrderT);
        if(count > 0){
            OrderVO orderVO = moocOrderTMapper.getOrderInfoById(uuid);
            if(orderVO == null || orderVO.getOrderId() == null){
                log.error("订单信息查询失败，订单编号为{}",uuid);
                return null;
            }else {

                return orderVO;
            }
        }else{
            log.error("创建订单失败");
            return null;
        }

    }

    private double getTotalPrice(int solds, double filmPrice){
        BigDecimal soldsDeci = new BigDecimal(solds);
        BigDecimal filmPriceDeci = new BigDecimal(filmPrice);
        BigDecimal result = soldsDeci.multiply(filmPriceDeci);

        //四舍五入，取小数点后两位
        BigDecimal bigDecimal = result.setScale(2, RoundingMode.HALF_UP);

        return bigDecimal.doubleValue();
    }

    @Override
    public Page<OrderVO> getOrderByUserId(Integer userId, Page<OrderVO> page) {
        Page<OrderVO> result = new Page<>();
        if(userId == null){
            log.error("订单查询业务失败，用户ID未传入");
            return  null;
        }

        List<OrderVO> orderVOList = moocOrderTMapper.getOrdersByUserId(userId,page);
        if(orderVOList == null || orderVOList.size() == 0){
            result.setTotal(0);
            result.setRecords(new ArrayList<>());
            return result;
        }else {

            EntityWrapper<MoocOrderT> entityWrapper = new EntityWrapper<>();
            entityWrapper.eq("order_user",userId);
            Integer integer = moocOrderTMapper.selectCount(entityWrapper);

            result.setTotal(integer);
            result.setRecords(orderVOList);
            return result;
        }
    }

    @Override
    public String getSoldSeatsByFieldId(Integer fieldId) {
        if(fieldId == null) {
            log.error("查询已售座位错误，未传入任何场次编号");
            return "";
        }else{
            return moocOrderTMapper.getSoldSeatsByFieldId(fieldId);
        }
    }

    @Override
    public OrderVO getOrderInfoById(String orderId) {

        OrderVO orderInfoById = moocOrderTMapper.getOrderInfoById(orderId);
        return orderInfoById;
    }

    @Override
    public boolean paySuccess(String orderId) {
        MoocOrderT moocOrderT = new MoocOrderT();
        moocOrderT.setOrderStatus(1);
        moocOrderT.setUuid(orderId);
        Integer integer = moocOrderTMapper.updateById(moocOrderT);
        if(integer >= 1){
            return true;
        }else{
            return false;
        }

    }

    @Override
    public boolean payFail(String orderId) {
        MoocOrderT moocOrderT = new MoocOrderT();
        moocOrderT.setOrderStatus(2);
        moocOrderT.setUuid(orderId);
        Integer integer = moocOrderTMapper.updateById(moocOrderT);
        if(integer >= 1){
            return true;
        }else{
            return false;
        }
    }
}
