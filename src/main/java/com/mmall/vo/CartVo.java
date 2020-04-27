package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Alex Cheng
 * 4/26/2020 11:06 AM
 */

//用户的购物车视图
public class CartVo {

    List<CartProductVo> cartProductVoList; //该用户全部购物车记录列表
    private BigDecimal cartTotalPrice; //购物车中总价
    private Boolean allChecked; //是否已经勾选，实时计算
    private String imageHost; // 购物车中也要显示一个图片，在ProductDetailVo中出现

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
