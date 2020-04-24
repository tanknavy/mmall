package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Alex Cheng
 * 4/23/2020 5:30 PM
 */

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(getClass());
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

    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){ //集合内0个元素
            logger.info("cannot find the sub-category from current category"); //没找到就log一下
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    // sub-category and deep query
    public ServerResponse selectCategoryAndChildrenById(Integer categoryId){
        //Set<Category> totalCategorySet = new HashSet<>();
        Set<Category> totalCategorySet = Sets.newHashSet(); //guawa
        findChildCategory(totalCategorySet, categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();
        if(categoryId != null){
            for(Category categoryItem: totalCategorySet){
                categoryIdList.add(categoryItem.getId());
            }
            //logger.info("cannot find the sub-category from current category"); //没找到就log一下
        }
        return ServerResponse.createBySuccess(categoryIdList); //返回category的Id，否则太大
    }

    // recursive function for above
    private Set<Category> findChildCategory(Set<Category> totalCategorySet, Integer categoryId){ // Category不是基本的类型，比如String(重写过),Integer,使用Set排重要重写hashCode和equals方法
        //三板斧，先判断退出条件？ 如下for loop中是null input, 它自己都不会执行，这就是return 条件
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null) {
            totalCategorySet.add(category); //只要有子节点就会被加入
            //return totalCategory
        } // else will not start cause the below loop will not start dur to empty category
        // sql: category.parentId in parentCategorySet
        //totalCategorySet.add(categoryMapper.selectByPrimaryKey(categoryId));
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        // if above is null, there is no loop below, this is end condition
        for (Category categoryItem : categoryList) {
            findChildCategory(totalCategorySet, categoryItem.getId());
        }
        return totalCategorySet;
    }

}
