package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/") // user登录接口
public class UserController {

    @Autowired
    private IUserService iUserService;

    // 登录接口
    @RequestMapping(value="login.do", method = RequestMethod.POST) //只接受post
    @ResponseBody //mvc的jackson插件,将自动将返回值转换为json
    public ServerResponse<User> login(String username, String password, HttpSession session){ //通用用户返回类型还未定义
        //controller->service->mybatis/dao->db
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER, response.getData()); //登录成功，设置session属性键值对currentUser:User w/o password
        }
        return response;
    }

    // 登出接口
    @RequestMapping(value = "logout.do",method = RequestMethod.POST) // get or post?
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){
        session.removeAttribute(Const.CURRENT_USER); //登出时设置移除当前session的用户属性
        return ServerResponse.createBySuccess();
    }

    // 注册接口
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){ //对象包含用户注册信息
        return iUserService.register(user);
    }

    //校验用户名还是邮箱地址
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type){
        return iUserService.checkValid(str,type);
    }

    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER); //session中
        if (user != null){
            return ServerResponse.createBySuccess(user); // ServerResponse<T>泛型体现
        } else{
            return ServerResponse.createByErrorMessage("User didn't login, cannot get user info");
        }
    }

    //
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }

    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer){
        return iUserService.checkAnswer(username, question, answer);
    }

    @RequestMapping(value = "forget_rest_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    // HttpSession判断登录状态
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew){
        User user = (User) session.getAttribute(Const.CURRENT_USER); //从前端request的session中判断用户
        if(user == null){
            return ServerResponse.createByErrorMessage("User didn't login!");
        }
        return iUserService.resetPassword(passwordOld, passwordNew, user);
    }

    //更新用户信息后，需要把User放在session里返回给前端，前端就可以直接更新在页面
    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session, User user){// user是front-end传过来待更新的info,
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER); //从前端request的session中判断用户
        if(currentUser == null){ //只有登录状态才能更新信息
            return ServerResponse.createByErrorMessage("User didn't login!");
        }
        user.setId(currentUser.getId()); //前端传过来的user没有id信息,设置一下
        user.setUsername(currentUser.getUsername()); //用户名不可以被更新，前端可能也没有传过来
        ServerResponse<User> response = iUserService.updateInformation(user);// DAO更新
        if (response.isSuccess()){ //如果后端更新成功
            response.getData().setUsername(currentUser.getUsername()); //后端不更新username,返回给front-end的数据中user设置为session中用户
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    //获取用户详细信息
    public ServerResponse<User> get_information(HttpSession session){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER); //从Object对象Cast到User
        if (currentUser == null){
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode(), "Need to login");
        }
        return iUserService.getInformation(currentUser.getId());
    }


}
