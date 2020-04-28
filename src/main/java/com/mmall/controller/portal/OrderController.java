package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Alex Cheng
 * 4/27/2020 12:16 PM
 */

// alibaba f2f pay
@Controller
@RequestMapping("/order/")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    // session用于用户登录判断，request用于拿到upload的文件路径，然后将QR传到FTP server，再返回给前端的二维码地址
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER); //cast Object->User
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload"); //返回tomcat上物理路径，用upload虚拟路径代表的
        System.out.println("===> getServletContext: " + path);
        //String path2 = session.getServletContext().getRealPath("upload"); // 可以合并使用吗？前端可能不会这样
        return iOrderService.pay(orderNo, user.getId(), path);
    }

    // Alipay callback回调方法，支付成功返回消息给我，继续我的业务处理流程，以及应答
    @RequestMapping("alipay_callback.do")
    @ResponseBody // alipay embed information into this request
    public Object alipayCallback(HttpServletRequest request){ //支付宝把回答放到request里面供我消费，会把按照alipay要的格式返回
        Map<String, String> params = Maps.newHashMap(); //接受支付宝的数据为我的格式

        Map requestParams = request.getParameterMap(); //string/string[], alipay return information
        for(Iterator iter = requestParams.keySet().iterator(); iter.hasNext();){ //通过迭代器迭代keyset, iterator loop
            String name = (String) iter.next(); //key
            String[] values = (String[]) requestParams.get(name);// value is String[]
            String valueStr = "";
            for(int i=0;i<values.length;i++){
                valueStr = (i==values.length-1)? valueStr + values[i] : values[i] + ",";//元素,分割拼接到一个字符串
            }
            params.put(name,valueStr);
        }
        //支付宝回调，签名，状态，参数
        logger.info("Alipay callback,sign:{},trade_status:{}, param:{}", params.get("sign"), params.get("trade_status"), params.toString());

        // 验证回调：is the caller alipay? 使用RSA验签方法，通过签名字符串，签名参数(base64解码)已经alipay public key
        // 还需验证: trade_no, amount, 注：除了sign and sign_type其它都是验证字符串(alipay sdk already remove sign)
        params.remove("sign_type"); //移除sign_type，剩下字符串拼接成RSA2 message
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(
                    params, Configs.getAlipayPublicKey(),"utf-8", Configs.getSignType());
            if(!alipayRSACheckedV2){ // 验证一：阿里签名验证失败
                return ServerResponse.createByErrorMessage("Illegal request, signature verified failed， call security police!");
            }
        } catch (AlipayApiException e) {
            logger.error("alipay callback error", e);
            e.printStackTrace();
        }

        // todo还需验证out_trade_no, amount，验证二


        // 上述两部验证都OK如果是true, 减少库存，更新订单状态等等
        ServerResponse serverResponse = iOrderService.aliCallback(params); // alipay获取的付款信息
        if(serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS; //send back to Alipay
        }
        return Const.AlipayCallback.RESPONSE_FAILED; //send back to Alipay

    }

    // front-end轮训查询订单支付转态， 支付成功的话，前台页面从支付QR二维码页面跳转到订单，和前端约定格式true/false
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER); //cast Object->User
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }




}
