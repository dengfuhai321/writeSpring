package com.xiaoden.demo.mvc.action;

import com.xiaoden.demo.service.IDemoService;
import com.xiaoden.spring.annotation.Autowried;
import com.xiaoden.spring.annotation.Controller;
import com.xiaoden.spring.annotation.RequestMapping;
import com.xiaoden.spring.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author dengfuhai
 * @description
 * @date 2019/9/26 0026
 */

@Controller
@RequestMapping("/demo")
public class DemoAction {

    @Autowried private IDemoService demoService;

    @RequestMapping("/query.json")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @RequestParam("name")String name){
    String result=demoService.get(name);
    System.out.println(result);
       /* try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
    @RequestMapping("/edit.json")
    public void edit(HttpServletRequest req, HttpServletResponse resp,Integer id){

    }
}
