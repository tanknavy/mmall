package com.mmall.service.test;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.impl.CategoryServiceImpl;
import com.mmall.service.impl.UserServiceImpl;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import java.util.List;

import static junit.framework.TestCase.assertEquals; //static method in package
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Alex Cheng
 * 4/23/2020 11:50 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class CategoryServiceImplTest {

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    //private MockMvc mockMvc;
    //@InjectMocks //注入mock的controller
    //private UserController userController;

    @Autowired //注入实现类
    private CategoryServiceImpl categoryService;

    /*
    @BeforeEach // junit 5
    public void setUp(WebApplicationContext wac) throws Exception{
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }*/

    @Test // test serviceImpl
    public void getChildrenParallelCategoryTest(){
        Integer categoryId = 100001; //0级品类
        //ServerResponse x = categoryService.getChildrenParallelCategory(categoryId); //子类
        ServerResponse x = categoryService.selectCategoryAndChildrenById(categoryId);//子孙类
        logger.info(ToStringBuilder.reflectionToString(x.getStatus()));
        //logger.info(ToStringBuilder.reflectionToString(x.getMsg())); //msg may null
        logger.info(ToStringBuilder.reflectionToString(x.getData())); //data may null

        List<Category> categories = (List) x.getData(); //返回列表类型
        assertEquals(6, categories.size()); // 断言0级下面品类数为5,全部子孙品类为30

        System.out.println("===>Server Response Status Code: " + x.getStatus());
        System.out.println(x.getData().toString()); // category detail
    }



}
