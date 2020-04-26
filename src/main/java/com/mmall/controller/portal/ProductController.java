package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Alex Cheng
 * 4/25/2020 5:19 PM
 */

// 给前端展示，页面香型，列表，搜索，动态排序
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId){
        return iProductService.getProductDetail(productId);
    }

    //@RequestParam控制关键字，可以不是必须的，mybatis的pageInfo帮助分页
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(
            @RequestParam(value="keyword",required = false) String keyword,
            @RequestParam(value="categoryId",required = false) Integer categoryId,
            @RequestParam(value="pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value="pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "orderBy",defaultValue = "") String orderBy){ //默认一页，每页10个

        return iProductService.getProductByKeywordCategory(keyword, categoryId, pageNum, pageSize, orderBy);

    }
}
