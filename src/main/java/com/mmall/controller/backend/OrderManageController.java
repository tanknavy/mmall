package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by Alex Cheng 4/28/2020 10:22 PM
 */

// order admin订单后台管理员
@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IUserService iUserService;

    //管理员订单列表
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpSession session,
                               @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){ //未付款的订单取消
        User user = (User) session.getAttribute(Const.CURRENT_USER); //cast Object->User
        //通过spring mvc把所有权限归一化
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            // business
            return iOrderService.manageList(pageNum, pageSize);

        }else {
            return ServerResponse.createByErrorMessage("No authorization to check order list");
        }
    }

    // 订单详情
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> orderDetail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER); //cast Object->User
        //通过spring mvc把所有权限归一化
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            // business
            return iOrderService.manageDetail(orderNo);

        }else {
            return ServerResponse.createByErrorMessage("No authorization to check order list");
        }
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderSearch(HttpSession session, Long orderNo,
                                               @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){ //未付款的订单取消){
        User user = (User) session.getAttribute(Const.CURRENT_USER); //cast Object->User
        //通过spring mvc把所有权限归一化
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            // business
            return iOrderService.manageSearch(orderNo, pageNum, pageSize);

        }else {
            return ServerResponse.createByErrorMessage("No authorization to check order list");
        }
    }

    // 后台发货
    // 订单详情
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> orderSendGoods(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER); //cast Object->User
        //通过spring mvc把所有权限归一化
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            // business
            return iOrderService.manageSendGoods(orderNo);

        }else {
            return ServerResponse.createByErrorMessage("No authorization to check order list");
        }
    }









}
