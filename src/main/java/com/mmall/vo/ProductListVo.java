package com.mmall.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Alex Cheng
 * 4/25/2020 10:28 AM
 */
public class ProductListVo {

    //copy from Product POJO, exclude some fields
    //清单而不是详情，ProductListVo像是ProductDetailVo的部分
    private Integer id;
    private Integer categoryId;

    private String name;
    private String subtitle;
    private String mainImage;
    //private String subImages;
    //private String detail;
    private BigDecimal price;
    //private Integer stock;
    private Integer status;
    //private Date createTime;
    //private Date updateTime;

    private String imageHost; //added

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
