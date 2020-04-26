package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by Alex Cheng
 * 4/23/2020 4:55 PM
 */

// 后台产品分类节点, 产品层级,0代表根节点
@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ICategoryService categoryService;

    @RequestMapping("/add_category.do")
    @ResponseBody
    public ServerResponse addCategory(
            HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId){ //如果前端没有传入id,默认为0
        User user = (User) session.getAttribute(Const.CURRENT_USER); // object 2 user
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "need to login");
        }
        if(userService.checkAdminRole(user).isSuccess()){
            //admin role, can maintain category
            return categoryService.addCategory(categoryName, parentId);

        }else{
            return ServerResponse.createByErrorMessage("need admin role");
        }
    }

    @RequestMapping("/set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session,Integer categoryId, String categoryName){
        User user = (User) session.getAttribute(Const.CURRENT_USER); // object 2 user
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "need to login");
        }
        if(userService.checkAdminRole(user).isSuccess()){
            //admin role, can maintain category
            return categoryService.updateCategoryName(categoryId, categoryName);

        }else{
            return ServerResponse.createByErrorMessage("need admin role");
        }
    }

    // 获取子节点平级信息，不递归
    @RequestMapping("/get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER); // object 2 user
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "need to login");
        }
        if(userService.checkAdminRole(user).isSuccess()){
            //admin role, can maintain category
            // 查询子节点信息，不递归
            return categoryService.getChildrenParallelCategory(categoryId);

        }else{
            return ServerResponse.createByErrorMessage("need admin role");
        }
    }

    // 获取子节点平级信息，递归
    @RequestMapping("/get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER); // object 2 user
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "need to login");
        }
        if(userService.checkAdminRole(user).isSuccess()){
            //admin role, can maintain category
            // 查询子节点信息，递归 0->10->100
            return categoryService.selectCategoryAndChildrenById(categoryId);

        }else{
            return ServerResponse.createByErrorMessage("need admin role");
        }
    }
}
