package com.xiaoden.demo.mvc.action;

import com.xiaoden.demo.service.IDemoService;
import com.xiaoden.spring.annotation.Autowried;
import com.xiaoden.spring.annotation.Controller;
import com.xiaoden.spring.annotation.RequestMapping;

/**
 * @author dengfuhai
 * @description
 * @date 2019/9/26 0026
 */
@Controller
public class MyAction {
    @Autowried
    IDemoService demoService;
    @RequestMapping("/index.html")
    public void query(){

    }

}
