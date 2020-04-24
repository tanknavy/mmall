package com.mmall.service.test;

import com.mmall.common.ServerResponse;

import com.mmall.pojo.User;
import com.mmall.service.impl.UserServiceImpl;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;
import static junit.framework.TestCase.assertEquals; //static method in package
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

/**
 * Created by Alex Cheng
 * 4/23/2020 11:50 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class UserServiceImplTest {

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    // test the implementation
    //private MockMvc mockMvc;
    //private IUserService iUserService;

    //@InjectMocks //注入mock的controller
    //private UserController userController;

    @Autowired //注入实现类
    private UserServiceImpl userService;

    /*
    @BeforeEach // junit 5
    public void setUp(WebApplicationContext wac) throws Exception{
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }*/

    @Test // test serviceImpl
    public void registerTest(){
        String testPassword = "123abcABC";
        //userService.checkPassword(testPassword);
        User user = new User();
        //user.setId(99);
        user.setUsername("AlexCheng");
        user.setPassword(testPassword);
        user.setEmail("cheng_alex@qqq.com");
        user.setPhone("6268071810");

        ServerResponse x = userService.register(user);
        //logger.info(ToStringBuilder.reflectionToString(x.getData())); //data may null
        logger.info(ToStringBuilder.reflectionToString(x.getMsg()));
        System.out.println(x.getMsg());
    }



}
