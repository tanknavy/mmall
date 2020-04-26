package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Alex Cheng
 * 4/24/2020 5:47 PM
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ICategoryService iCategoryService; //service call service

    // backend for admin
    // product insert/update,对后端都一样
    @Override
    public ServerResponse saveOrUpdateProduct(Product product){
        if(product !=null){
            if(StringUtils.isNotBlank(product.getSubImages())){
                String[] subImageArray = product.getSubImages().split(",");//和前端格式约定
                if(subImageArray.length > 0){
                    product.setMainImage(subImageArray[0]);
                }
            }
            if(product.getId() != null){ //前端包含productId，就是更新，否则就是新增
                int rowCount = productMapper.updateByPrimaryKey(product);
                if(rowCount > 0){
                    return ServerResponse.createBySuccessMessage("product update success!");
                }
                return ServerResponse.createByErrorMessage("product update failed!");
            }else{
                int rowCount = productMapper.insert(product);
                if(rowCount > 0){
                    return ServerResponse.createBySuccessMessage("product add success!");
                }
                return ServerResponse.createByErrorMessage("product add failed!");
            }
        }
        return ServerResponse.createByErrorMessage("product parameter error!");
    }

    @Override
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status){
        if(productId == null || status == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product); //选择性更新
        if(rowCount >0){
            return ServerResponse.createBySuccessMessage("product sales status update success");
        }
        return ServerResponse.createByErrorMessage("product sales status update failed");
    }

    @Override
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId){
        if(productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("product is out of stock or deleted!");
        }
        // VO(value object),承载各个值的对象，POJO->VO, 复杂的业务：POJO->BO(business object)->VO(view object)
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        //below fields don't exist in POJO,
        // imageHost,配置和代码分离，读取image配置文件, 从工具类中使用static静态代码块获取
        // ftp.server.http.prefix=http://img.tanknavy.com/
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "ftp.server.http.prefix"));

        // parentCategoryId
        //本产品的category id,还可以查询parent categoryId, 无需join，在业务逻辑中条件查询
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0); //默认赋值为0，就是根节点，一级品类
        }else{
            productDetailVo.setParentCategoryId(category.getParentId()); //set parent categoryId
        }

        // createTime & updateTime, exist in POJO/DB, it's datetime type,通过mybatis拿到是timestamp毫秒数
        // 将从DB中返回的时间在VO里面转成String格式时间
        productDetailVo.setCreateTime(DateTimeUtil.date2Str(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.date2Str(product.getUpdateTime()));

        return productDetailVo;
    }

    @Override
    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize){
        //pageHelper使用3步骤
        // 1)startPage -- start记录一个开始
        // 2)填充sql查询逻辑,mybatis才可以针对结果和pageNum,pageSize设定计算
        // 3)pageHelper收尾
        PageHelper.startPage(pageNum, pageSize); //很多重载方法，比如是否count查询，是否排序orderBy

        List<Product> productList = productMapper.selectList(); //pageHelper所需sql查询，这里SQL用了全选？

        //需要一个vo,清单而不是详情，ProductListVo像是ProductDetailVo的部分
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem:productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList); //需要返回给前台展示的实际清单数据

        return ServerResponse.createBySuccess(pageResult);

    }

    private ProductListVo assembleProductListVo(Product product){ //组装方法
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        // add one column
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage()); //list页要有主图
        productListVo.setPrice(product.getPrice()); //价格
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());

        return productListVo;
    }

    //产品搜索，同上但有查询条件
    @Override
    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize){
        //pageHelper使用3步骤
        // 1)startPage -- start记录一个开始
        // 2)填充sql查询逻辑,mybatis才可以针对结果和pageNum,pageSize设定计算
        // 3)pageHelper收尾
        PageHelper.startPage(pageNum, pageSize);
        if(StringUtils.isNotBlank(productName)){
            // sql query productName like %productName%
            productName = new StringBuilder().append("%").append(productName).append("%").toString(); //字符串拼接
        }

        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);
        //需要一个vo,清单而不是详情，ProductListVo像是ProductDetailVo的部分
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem:productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        PageInfo pageResult = new PageInfo(productList);//开始分页
        pageResult.setList(productListVoList); //需要返回给前台展示的实际清单数据

        return ServerResponse.createBySuccess(pageResult);

    }

    // portal for front-end customer

    // 类似上面manageProductDetail
    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){
        if(productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("product is out of stock or deleted!");
        }
        //与admin角色相比，需要判断是否在售状态
        if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("product is out of stock 商品已下架");
        }

        // VO(value object),承载各个值的对象，POJO->VO, 复杂的业务：POJO->BO(business object)->VO(view object)
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    // 搜索，分页
    @Override
    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId, int pageNum, int pageSize, String orderBy){
        if(StringUtils.isBlank(keyword) && categoryId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        List<Integer> categoryIdList = new ArrayList<>();

        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank(keyword)){
                //没有分类，也没有关键字，返回空结果集，不报错
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                //pageInfo.setList(); //list没有变化无需这步骤
                return ServerResponse.createBySuccess(pageInfo);

            }else{
                //根据categoryId递归搜索，拿到全部id,后续再查询product
                categoryIdList = iCategoryService.selectCategoryAndChildrenById(categoryId).getData();
            }
        }


        if(StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        //排序处理 PageHelper的orderBy准备
        if(StringUtils.isNotBlank(orderBy)){ //默认按照价格排序
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){ //"price_desc","price_asc"
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]); // "price desc",方法要求的参数格式
            }
        }

        // keyword如果为""，就设置为null, Mapper里面会判断null, 否则传入""造成问题
        // categoryIdList也要判断一下，否则在sql里面where id in ""造成问题
        List<Product> productList = productMapper.selectByNameAndCategoryIds(
                StringUtils.isBlank(keyword)? null:keyword,
                categoryIdList.size() ==0? null: categoryIdList);//为了不传入""或者0个元素的集合，设为null

        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem:productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        //开始分页
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);

        return ServerResponse.createBySuccess(pageInfo);


    }



}
