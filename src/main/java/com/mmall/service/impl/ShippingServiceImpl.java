package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Alex Cheng
 * 4/26/2020 6:16 PM
 */

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping); //使用mybatis的userGeneratedKey & keyProperty拿到自增键
        if(rowCount >0){
            Map result = Maps.newHashMap(); //使用map装载这个数据
            result.put("shippingId", shipping.getId()); //上面insert后mybatis可以自动拿到DB返回的id
            return ServerResponse.createBySuccessMessage("Address added success", result);
        }
        return ServerResponse.createByErrorMessage("Address added failed!");
    }

    public ServerResponse del(Integer userId, Integer shippingId){
        //int resultCount = shippingMapper.deleteByPrimaryKey(shippingId); //防止横向越权，登录后，传入的不是自己的一个shippingId
        int resultCount = shippingMapper.deleteByShippingIdUserId(userId,shippingId); //防止横向越权，登录后，传入的不是自己的一个shippingId
        if(resultCount >0){
            return ServerResponse.createBySuccessMessage("Address delete success");
        }
        return ServerResponse.createByErrorMessage("Address delete failed!");
    }

    public ServerResponse update(Integer userId, Shipping shipping){
        shipping.setUserId(userId); //防止横向越权，防止shipping中传过来userId是假的
        int rowCount = shippingMapper.updateByShipping(shipping); //选择性更新
        if(rowCount >0){
            return ServerResponse.createBySuccessMessage("Address update success"); //update不需要返回data给前端
        }
        return ServerResponse.createByErrorMessage("Address update failed!");
    }

    //查单个shipping的详情
    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId, shippingId); //查询时也要判断越权的问题
        if(shipping == null){
            return ServerResponse.createByErrorMessage("Cannot found this address"); //update不需要返回data给前端
        }
        return ServerResponse.createBySuccessMessage("Address update failed!", shipping);
    }

    //单个用户下全部shipping并分页结果
    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum, pageSize);

        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);

        PageInfo pageInfo = new PageInfo(shippingList);
        //pageInfo.setList(shippingList); //数据没有变动无需重新set

        return ServerResponse.createBySuccess(pageInfo);
    }


}
