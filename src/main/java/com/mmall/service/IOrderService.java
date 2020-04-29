package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

public interface IOrderService {
    ServerResponse pay(Long orderNo, Integer userId, String path);
    ServerResponse aliCallback(Map<String, String> params);
    ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);
    ServerResponse<OrderVo> createOrder(Integer userId, Integer shippingId);
}
