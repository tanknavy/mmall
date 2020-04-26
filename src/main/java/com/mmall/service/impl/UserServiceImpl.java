package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

@Service("iUserService") //注解
public class UserServiceImpl implements IUserService {

    @Autowired // 如果idea报错bean找不到，修改idea的错误为warning
    private UserMapper userMapper; // DAO的实现，由mybatis的Mapper.xml完成

    //登录
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username); //登录的用户名
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("User doesn't exist");
        }
        // 密码登录MD5,比较加密后的
        String md5Password = MD5Util.MD5EncodeUtf8(password);

        // 用户名密码匹配
        User user = userMapper.selectLogin(username,md5Password);
        if (user == null){
            return ServerResponse.createByErrorMessage("Password error");
        }

        user.setPassword(StringUtils.EMPTY); //DB层用户密码匹配，所以这里将用户密码置空返回
        return ServerResponse.createBySuccessMessage("login successful", user);
    }

    //注册
    @Override
    public ServerResponse<String> register(User user){
        //用户名和邮件是否存在, 确保唯一
        /*
        int resultCount = userMapper.checkUsername(user.getUsername());
        if (resultCount > 0 ){
            return ServerResponse.createByErrorMessage("User already registered!");
        }
        resultCount = userMapper.checkEmail(user.getEmail());
        if (resultCount > 0 ){
            return ServerResponse.createByErrorMessage("Email already exist!");
        }*/
        ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if(!validResponse.isSuccess()){ //用户名已存在
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if(!validResponse.isSuccess()){ // 邮箱已存在
            return validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);//默认customer类型
        // md5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        //resultCount = userMapper.insert(user); //写入DB
        int resultCount = userMapper.insert(user); //写入DB
        if (resultCount == 0){ //假如DB插入返回异常
            return ServerResponse.createByErrorMessage("Register failed!");
        }

        return ServerResponse.createBySuccessMessage("User Register is successful");

    }

    // 注册时用户名和邮箱校验，希望都不存在
    @Override
    public ServerResponse<String> checkValid(String str, String type){
        // 不为null且不为" "
        if(StringUtils.isNotBlank(type)){ //" "这个isNotEmpty=true, isNotBlank=false
            // 开始校验
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str); //登录的用户名
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("User already exist");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str); //登录的邮箱
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("Email already exist");
                }
            }

        }else{
            return ServerResponse.createByErrorMessage("error parameter!");
        }

        return ServerResponse.createBySuccessMessage("User validation success"); //用户和邮箱都不存在，可以后续注册
    }

    @Override
    public ServerResponse<String> selectQuestion(String username){
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if(validResponse.isSuccess()){ //用户不存在
            return ServerResponse.createByErrorMessage("user doesn't exist");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccessMessage(question);
        }
        return ServerResponse.createByErrorMessage("User question is blank");
    }

    //使用本地缓存检查用户问题答案
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer){
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if (resultCount > 0){ //question/answer match
            String forgetToken = UUID.randomUUID().toString(); // 申明一个token,89bfd661-e20c-4b91-8e34-552795e12df9,重复概率极低
            // token放到本地cache中，并设置有效期
            TokenCache.setKey("token_" + username, forgetToken); //本地缓存用户uuid, token_username/uuid
            return ServerResponse.createBySuccess(forgetToken);//返回uuid token给前端
        }
        return ServerResponse.createByErrorMessage("Answer is not correct");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        if (StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("parameter error, need uuid token");
        }
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if(validResponse.isSuccess()){ //用户不存在
            return validResponse;
        }

        String token = TokenCache.getKey("token_" + username); //本地缓存中的token
        // check uuid from frond-ent and server cache
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token invalided or expired ");
        }

        // token from front-end compare token from local server cache
        if(StringUtils.equals(forgetToken, token)){ //StringUtils判断比较安全，它会先null判断
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew); //MD5 password in DB
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password); // update SQL会返回受影响行数

            if(rowCount >0){
                return ServerResponse.createBySuccessMessage("password update success");
            }
        }else{
            return ServerResponse.createByErrorMessage("token error, please request new token");
        }

        return ServerResponse.createByErrorMessage("password update failed!");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user){
        // 防止横向越权，要校验一下这个用户的旧密码一定要制定是这个用户，因为查询count(1)，如果不指定用户id，结果就是count>0
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if( resultCount == 0){
            return ServerResponse.createByErrorMessage("old password is incorrect");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        // SQL中加有Selective会检查每个输入栏位，不为空的才更新
        int updateCount = userMapper.updateByPrimaryKeySelective(user);//根据主键有选择性的更新
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("password update success");
        }
        return ServerResponse.createByErrorMessage("password update failed!");
    }

    @Override
    public ServerResponse<User> updateInformation(User user){
        //username 不能更新，并且email也要校验，不能存在，如果存在不能是当前用户，这里容许一个多个用户名公用一个email
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if(resultCount >0 ){
            return ServerResponse.createByErrorMessage("email exist, please change and try again");
        }
        User updateUser = new User(); //user不能更新，email需要先校验
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("update user information success", updateUser);
        }
        return ServerResponse.createByErrorMessage("update user information failed!");

    }

    @Override
    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("Cannot find current user");
        }
        user.setPassword(StringUtils.EMPTY); //返回的用户password栏位要置空即""
        return ServerResponse.createBySuccess(user);
    }

    // password compliance check
    public Boolean checkPassword(String password){
        final String PASSWORD_REGEX = Const.PASSWORD_RULE;
        final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

        return PASSWORD_PATTERN.matcher(password).matches();

    }

    // backend
    @Override
    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}
