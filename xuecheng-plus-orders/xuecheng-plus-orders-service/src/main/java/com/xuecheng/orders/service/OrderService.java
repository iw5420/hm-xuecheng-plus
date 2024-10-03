package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;

public interface OrderService {

    /**
     * @description 創建商品訂單
     * @param addOrderDto 訂單信息
     * @return PayRecordDto 支付交易記錄(包括二維碼)
     * @author Mr.M
     * @date 2022/10/4 11:02
     */
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

}
