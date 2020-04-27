package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Alex Cheng
 * 4/26/2020 10:31 AM
 */

//购物车问题；价格BigDecimal, 库存校验
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId); //该用户，该产品的一条购物车记录
        if (cart == null) {
            //该产品不在购物车里，需要新增一个产品记录
            Cart cartItem = new Cart();
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setUserId(userId);

            cartMapper.insert(cartItem);//新增一条该用户对该产品的shopping cart记录
        } else { //该产品已经在购物车里面了，数量相加
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKey(cart);//更新一下记录
        }
        CartVo cartVo = getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId); //该用户，该产品的一条购物车记录
        if (cart != null) {
            //该产品在购物车里，可以做更新
            //Cart cartItem = new Cart();
            cart.setQuantity(count); //设置数量
        } //else { }//该产品不在购物车，这种情况会发生吗？

        cartMapper.updateByPrimaryKeySelective(cart);//更新一下记录
        CartVo cartVo = getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds) { //要删除的产品列表
        //使用guava的split方法，直接split成list
        List<String> productList = Splitter.on(",").splitToList(productIds); //逗号分割的字符串
        if (CollectionUtils.isEmpty(productList)) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId, productList); //该用户，该产品的一条购物车记录
        CartVo cartVo = getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> list(Integer userId) { //要删除的产品列表
        CartVo cartVo = getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo); //其它代码复用一下
    }

    /*
    public ServerResponse<CartVo> selectOrUnSelectAll(Integer userId, Integer checked) { //要删除的产品列表
        cartMapper.checkedOrUnCheckedAllProduct(userId, checked);
        return this.list(userId);
    }*/

    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked) { //要删除的产品列表
        cartMapper.checkedOrUncheckedProduct(userId, productId, checked);
        return this.list(userId);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if(userId == null){
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }


    // 用户购物车核心方法，从DB中的cart,product表获取实时数据，然后assemble成CartVo返回
    private CartVo getCartVoLimit(Integer userId) { // cart and product
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();//cart & product的结合

        BigDecimal cartTotalPrice = new BigDecimal("0");//选择string构造器初始化总价

        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo();

                // cartProductVo从cart中获取的数据
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());

                // cartProductVo从product中获取的数据
                // 设计思想 1)不在DB层涉及join,trigger,完全在business logic层实现，
                // 2) 在DB单表查询，在业务层assemble数据返回给front-end
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock()); //商品库存
                    //判断库存
                    int buyLimitCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()) { //如果库存量>=购物车购买数量
                        // 库存充足
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUN_SUCCESS);
                    } else {
                        buyLimitCount = product.getStock(); //如果超出库存，就要更新cart对象该用户该产品的购买量
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL); //购买超过库存，设置最大购买量
                        //购物车中更新有效库存到DB
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);//选择性更新
                    }
                    // view展示数据,继续set
                    cartProductVo.setQuantity(buyLimitCount);
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), buyLimitCount)); //购物车中某类产品总价
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                //产品在购物车中是否被选中
                if (cartItem.getChecked() == Const.Cart.CHECKED) {
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                }

                cartProductVoList.add(cartProductVo);

            }
        }

        //如果购物车为空呢？也要显示购物车CartVo,不是错误，而是空的
        //Cart POJO->CartProductVo->CartVo,用户的购物车
        cartVo.setCartTotalPrice(cartTotalPrice); //购物车总价
        cartVo.setCartProductVoList(cartProductVoList); //用户购物车产品列表
        cartVo.setAllChecked(getAllCheckedStatus(userId)); //前端根据这个决定是否显示全部勾选
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));//产品图片主机,假如多个主机呢？

        return cartVo;
    }

    //shopping cart中用户是否商品全部勾选了
    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0; //是否全选
    }


}
