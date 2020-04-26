package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by Alex Cheng
 * 4/24/2020 5:39 PM
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"Need to login");
        }
        if(userService.checkAdminRole(user).isSuccess()){
            // maintain product logic
            return iProductService.saveOrUpdateProduct(product);
        }else{
            return ServerResponse.createByErrorMessage("No authorization to maintain");
        }
    }

    // 产品上下架
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"Need to login");
        }
        if(userService.checkAdminRole(user).isSuccess()){
            // maintain product logic
            return iProductService.setSaleStatus(productId,status);
        }else{
            return ServerResponse.createByErrorMessage("No authorization to maintain");
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"Need to login");
        }
        if(userService.checkAdminRole(user).isSuccess()){
            // business logic, 业务逻辑
            return iProductService.manageProductDetail(productId); //使用VO
        }else{
            return ServerResponse.createByErrorMessage("No authorization to maintain");
        }
    }

    // 后台产品(全)查询list分页, 前台搜索动态排序，使用mybatis的pageelper采用AOP监听，然后再次执行就可以实现分页
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session,
                                  @RequestParam(value = "pageNum", defaultValue = "1" ) int pageNum, //RequestParam设置默认值
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){ //RequestParam设置默认值
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"Need to login");
        }
        if(userService.checkAdminRole(user).isSuccess()){
            // business logic, 业务逻辑
            return iProductService.getProductList(pageNum, pageSize);

        }else{
            return ServerResponse.createByErrorMessage("No authorization to maintain");
        }
    }

    // 后台产品(条件)搜索list分页, 前台搜索动态排序，使用mybatis的pageelper采用AOP监听，然后再次执行就可以实现分页
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, String productName, Integer productId,
                                  @RequestParam(value = "pageNum", defaultValue = "1" ) int pageNum, //RequestParam设置默认值
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){ //RequestParam设置默认值
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"Need to login");
        }
        if(userService.checkAdminRole(user).isSuccess()){
            // business logic, 业务逻辑
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);

        }else{
            return ServerResponse.createByErrorMessage("No authorization to maintain");
        }
    }


    //后台编辑产品的时候，spring mvn上传产品图片到tomcat服务器,->ftp server ->在tomcat上删除源文件
    //在webapp的WEB-INF下面有spring mvn关于文件上传配置的xml文件，最大文件大小，内存处理大小，utf8编码
    //可以在jsp中测试
    // 既然request包含session,为啥为分开？
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"Need to login");
        }
        if(userService.checkAdminRole(user).isSuccess()){
            // business logic, 业务逻辑
            //当前request的session（没有就创建）,Servlet上下文(context root)，然后拿到tomcat下真实路径
            String path = request.getSession().getServletContext().getRealPath("upload"); //在webapp/下
            String targetFileName = iFileService.upload(file, path); //文件被uuid重命名，上传到ftp,然后tomcat下删除, test failed
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName; // http://img.tanknavy.com/ + fileName

            Map fileMap = Maps.newHashMap();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);
            return ServerResponse.createBySuccess(fileMap);

        }else{
            return ServerResponse.createByErrorMessage("No authorization to maintain");
        }
    }

    //rich text富文本上传
    //富文本中对于返回值有自己的要求，使用simditor,所以按照simditor的要求进行返回
        /*
        {
            "success": true/false,
            "msg":"error message", # optional
            "file_path": "[real file path]"
        }*/
    //后台编辑产品的时候，spring mvn上传产品图片到tomcat服务器,->ftp server ->在tomcat上删除源文件
    //在webapp的WEB-INF下面有spring mvn关于文件上传配置的xml文件，最大文件大小，内存处理大小，utf8编码
    //可以在jsp中测试
    @RequestMapping("richtext_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        Map resultMap = Maps.newHashMap(); //simditor格式返回
        if(user == null){
            resultMap.put("success",false);
            resultMap.put("msg","Need to login as Admin");
            return resultMap;
            //return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"Need to login");
        }

        if(userService.checkAdminRole(user).isSuccess()){
            // business logic, 业务逻辑
            //当前request的session（没有就创建）,Servlet上下文(context root)，然后拿到tomcat下真实路径
            String path = request.getSession().getServletContext().getRealPath("upload"); //在webapp/下
            String targetFileName = iFileService.upload(file, path); //文件被uuid重命名，上传到ftp,然后tomcat下删除

            if(StringUtils.isNotBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","No authorization to maintain");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName; // http://img.tanknavy.com/ + fileName

            resultMap.put("success",true);
            resultMap.put("msg","success to upload");
            resultMap.put("file_path",url);

            //front-end要求，返回response 的header修改
            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
            return resultMap;

        }else{
            //return ServerResponse.createByErrorMessage("No authorization to maintain");
            resultMap.put("success",false);
            resultMap.put("msg","No authorization to maintain");
            return resultMap;
        }
    }


}

