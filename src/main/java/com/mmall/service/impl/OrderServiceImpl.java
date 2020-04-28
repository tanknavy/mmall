package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.OrderItemMapper;
import com.mmall.dao.OrderMapper;
import com.mmall.dao.PayInfoMapper;
import com.mmall.pojo.Order;
import com.mmall.pojo.OrderItem;
import com.mmall.pojo.PayInfo;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mysql.fabric.Server;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Alex Cheng
 * 4/27/2020 12:26 PM
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static  AlipayTradeService tradeService;
    static {

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;

    private static Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    public ServerResponse pay(Long orderNo, Integer userId, String path){ //订单，用户，二维码存放路劲
        Map<String, String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("User has no this order");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        //参考alipay如果生成支付二维码的流程
        // 测试当面付2.0生成支付二维码
        //main.test_trade_precreate();
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        //String outTradeNo = "tradeprecreate" + System.currentTimeMillis() + (long) (Math.random() * 10000000L);
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        //String subject = "xxx品牌xxx门店当面付扫码消费";
        String subject = new StringBuilder().append("emall f2f scanner pay, orderNo: ").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        //String totalAmount = "0.01";
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        //String body = "购买商品3件共20.00元";
        String body = new StringBuilder().append("Order No: ").append(outTradeNo).append("TotalAmount: ").append(totalAmount).append("!").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo, userId);
        for(OrderItem orderItem:orderItemList){ //price unit is cent,not $xxx.xx, so price * 100
            GoodsDetail goods = GoodsDetail.newInstance(
                    orderItem.getProductId().toString(),
                    orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goods);
        }

        /*
        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx小面包", 1000, 1);
        // 创建好一个商品后添加至商品明细列表
        goodsDetailList.add(goods1);

        // 继续创建并添加第一条商品信息，用户购买的产品为“黑人牙刷”，单价为5.00元，购买了两件
        GoodsDetail goods2 = GoodsDetail.newInstance("goods_id002", "xxx牙刷", 500, 2);
        goodsDetailList.add(goods2);
        */
        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                //                .setNotifyUrl("http://www.test-notify-url.com")//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url", "www.tanknavy.com"))
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if(!folder.exists()){ // 如果目录(tomcat上)不存在
                    folder.setWritable(true);
                    folder.mkdirs();
                }
                // 需要修改为运行机器上的路径
                // 使用zxing生成二维码，临时保存在tomcat服务器上，然后上传到FTP server
                //String filePath = String.format("/Users/sudo/Desktop/qr-%s.png", response.getOutTradeNo());
                String filePath = String.format(path + "/qr-%s.png", response.getOutTradeNo()); //路径细节,QR文件名
                String qrFileName = String.format("qr-%.png", response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath); //使用google的zxing作为二维码生成工具

                File targetFile = new File(path, qrFileName); //将QR文件从tomcat移到FTP文件服务器上
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("upload QR file to FTP error", e);
                    //e.printStackTrace();
                }

                log.info("filePath:" + filePath); //
                //                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName(); //FTP服务器上路径
                resultMap.put("qrUrl",qrUrl); //准备返回给front-end

                return ServerResponse.createBySuccess(resultMap);
                //break;

            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServerResponse.createBySuccessMessage("Alipay payment failed!");
                //break;

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createBySuccessMessage("Alipay payment failed!");
                //break;

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createBySuccessMessage("Alipay payment failed!");
                //break;
        }

    }


    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }

    // 处理支付宝回调，如果支付确认成功，更新订单转态，创建支付记录
    public ServerResponse aliCallback(Map<String, String> params){
        Long orderNo = Long.parseLong(params.get("out_trade_no")); //支付宝从传过来的订单号，也就是我最初传过去的内部订单号
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("not exist this order Number");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){ //订单已经发货/关闭,无需处理
            return ServerResponse.createBySuccessMessage("Alipay duplicated call");
        }

        //支付宝金额和内部订单金额是否一致,Number(9,2),怎么判断
        BigDecimal aliAmount = new BigDecimal(params.get("total_amount"));
        if(order.getPayment().doubleValue() != aliAmount.doubleValue()){ //金额不等
                return ServerResponse.createByErrorMessage("Payment Amount doesn't match");
        }

        if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.str2Date(params.get("gmt_payment"))); //从支付宝信息中读取支付时间戳"yyyy-MM-dd HH:mm:ss"
            order.setStatus(Const.OrderStatusEnum.PAID.getCode()); //更新订单状态
            orderMapper.updateByPrimaryKeySelective(order);//选择性更新到DB
        }

        PayInfo payInfo = new PayInfo(); // 创建支付记录
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);//执行写入DB

        return ServerResponse.createBySuccessMessage("payment success");

    }

    //before aliCallback，the message signature ID is verified, continue to verify the out_trade_no and amount match with internal order


    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("No this order");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess(); //前台只需知道true/false
        }
        return ServerResponse.createByError();

    }





}