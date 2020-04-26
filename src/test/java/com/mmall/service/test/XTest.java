package com.mmall.service.test;

import com.google.common.collect.Sets;
import com.mmall.pojo.Category;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

/**
 * Created by Alex Cheng
 * 4/24/2020 12:20 PM
 */
public class XTest {
    public static void main(String[] args) {
        Set<Object> theSet = Sets.newHashSet();
        theSet.add("aa");
        theSet.add("bb");
        theSet.add(null);

        System.out.println(theSet.toString());
        System.out.println(theSet.size());

        myMethod(2, 10);
    }

    public static Integer myMethod(@RequestParam(value = "var1", defaultValue = "1") int var1, int var2){
        return var1 * var2;
    }
}
