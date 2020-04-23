package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    // 上面是mybatis生成器自动产生，这里开始根据需求写
    int checkUsername(String username);//检查用户是否存在DB中

    int checkEmail(String email);

    // mybatis在传递多个参数是需要@Param注解
    User selectLogin(@Param("username") String username, @Param("password") String password); //用户密码对查询

    // user password question
    String selectQuestionByUsername(String username);

    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    int updatePasswordByUsername(@Param("username")String username, @Param("passwordNew")String passwordNew);

    int checkPassword(@Param("password")String password, @Param("userId")Integer userId);

    int checkEmailByUserId(@Param("email") String email, @Param("userId") Integer userId);
}