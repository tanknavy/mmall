package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Alex Cheng
 * 4/23/2020 5:30 PM
 */

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId){
        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("parameter error");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        int rowCount = categoryMapper.insert(category);//返回sql生效行数
        if(rowCount >0){
            return ServerResponse.createBySuccessMessage("Category added success");
        }

        return ServerResponse.createByErrorMessage("Category added failed!");
    }

    @Override
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName){
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("parameter error");
        }
        Category category = new Category();
        category.setId(categoryId); //为了方便以后按照主键更新
        category.setName(categoryName);

        // 有选择性的更新，按照主键
        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount >0){
            return ServerResponse.createBySuccessMessage("Category updated success");
        }
        return ServerResponse.createByErrorMessage("Category updated failed!");
    }
}
