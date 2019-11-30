package com.stylefeng.guns.api.alipay;

import com.stylefeng.guns.api.alipay.vo.AlipayInfoVO;
import com.stylefeng.guns.api.alipay.vo.AlipayResultVO;

public interface AlipayAPI {

    AlipayInfoVO getQRCode(String orderId);

    AlipayResultVO getOrderStatus(String orderId);


}
