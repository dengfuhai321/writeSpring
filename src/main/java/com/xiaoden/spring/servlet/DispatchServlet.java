package com.xiaoden.spring.servlet;

import com.xiaoden.demo.mvc.action.DemoAction;
import com.xiaoden.spring.annotation.Autowried;
import com.xiaoden.spring.annotation.Controller;
import com.xiaoden.spring.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author dengfuhai
 * @description
 * @date 2019/9/25 0025
 */
//servlet只是作为一个mvc的启动入口
public class DispatchServlet extends HttpServlet {

    private Properties contextConfig=new Properties();

    private Map<String,Object> beanMap=new ConcurrentHashMap<>();
    //包全名
    private List<String> classNames=new ArrayList<>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("------------执行doPost方法-------------");
    }

    @Override
    //<load-on-startup>1只要大于1应用启动时就会加载这个servlet
    //在web项目发布的时候该servlet会被web.xml注册，也就是自动运行了该init初始化方法
    public void init(ServletConfig config) throws ServletException {
        //开始初始化的进程

        //定位
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //加载
        doScanner(contextConfig.getProperty("scanPackage"));

        //注册
        doRegistry();

        //自动依赖注入
        //Spring中如果不设置懒加载=false,就不会主动依赖注入，只有在getBean时才会依赖注入
        doAutowired();

        //测试看以上步骤是否成功
        DemoAction demoAction=(DemoAction)beanMap.get("demoAction");
        demoAction.query(null,null,"xiaodeng");
        //如果是springmvc会多设计一个HandleMapping

        //将@RequestMapping中配置的url和一个Method关联上
        //便于从浏览器获得用户输入url以后，能够找到具体执行的Method
        initHandleMapping();
    }

    private void initHandleMapping() {
    }

    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());//得到一个目录文件
        for (File file:classDir.listFiles()) {//遍历目录中的文件
            if (file.isDirectory()){//如果是目录就遍历目录中的文件再加载一下
                doScanner(packageName+"."+file.getName());
            }else{
                classNames.add(packageName+"."+file.getName().replace(".class",""));
            }
            
        }
    }

    private void doLoadConfig(String location) {

        //从当前类所在包下加载指定名称的文件，getClass是到当前列
        // InputStream in = this.getClass().getResourceAsStream("biabc.properties");
        // 从classpath根目录下加载指定名称的文件，这是因为/即代表根目录
// InputStream in = this.getClass().getResourceAsStream("/abc.properties");
// 从classpath根目录下加载指定名称的文件，这是因为getClassLoader就会到根目录上
// InputStream in = this.getClass().getClassLoader().getResourceAsStream("abc.properties");


        //在spring中是通过Reader查找和定位的
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(location.replace("classpath:",""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doRegistry() {
        if(classNames.isEmpty()){
            return ;
        }
        try {
        for (String className:classNames){

            Class<?> clazz = Class.forName(className);
            //如果该类有Controller这个注解
            //在spring中，是用的多个子方法来执行的，比如parseArray,parseMap
            if(clazz.isAnnotationPresent(Controller.class)){
                //getName ----“实体名称” ---- com.se7en.test.Test
                //getSimpleName ---- “底层类简称” ---- Test
                String beanName = lowerFirstCase(clazz.getSimpleName());
                //放入ioc容器中，spring中不会直接放入实例bean，放入的是BeanDefinition
                beanMap.put(beanName,clazz.newInstance());
            //判断如果是带有Service这个注解，就执行下面的步骤
            }else if (clazz.isAnnotationPresent(Service.class)){
                Service service = clazz.getAnnotation(Service.class);

                //默认用类名首字母注入
                //如果自己定义了beanName,那么优先使用自己定义的BeanName
                //如果是一个接口，使用接口的类型自动注入
                //spring中同样分别调用不同的方法注入autowireByName autowireByType
                String beanName = service.value();
                //如果没有给Service专门起名字，就直接去类名首字母小写
                if("".equals(beanName.trim())){//去掉字符串首尾的空格
                    beanName=lowerFirstCase(clazz.getSimpleName());
                }
                Object instance=clazz.newInstance();
                beanMap.put(beanName,instance);
                //如果是一个接口怎么办
                Class<?>[] interfaces = clazz.getInterfaces();
                for ( Class<?>i :interfaces) {
                    beanMap.put(i.getName(),instance);
                }


            }else{//啥注解也没有，就继续循环
                continue;
            }

        }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAutowired() {
        if(beanMap.isEmpty()){
            return ;
        }
        for (Map.Entry<String,Object> entry:beanMap.entrySet()) {
            Field[] Fields = entry.getValue().getClass().getDeclaredFields();
            for(Field field:Fields){
                //如果没有加自动注入就不注入该属性，continue
                if(!field.isAnnotationPresent(Autowried.class)){
                    continue;
                }
                Autowried annotation = field.getAnnotation(Autowried.class);
                //获取设置的Autowired的名字
                String beanName = annotation.value().trim();
                //如果没有设置就取属性名字
                if("".equals(beanName)){//---这里应该直接getname()
                    //这里其实不管是得到接口的名字还是实现接口的类名字，都得到的是实现类
                    //因为之前往beanMap中存入 接口的value还是具体的实现类
                    beanName=field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    //这里完成给对象自动注入对象属性
                    field.set(entry.getValue(),beanMap.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    private String lowerFirstCase(String str){
        char[] chars = str.toCharArray();
        chars[0] +=32;//把子一个字母变成小写
        return String.valueOf(chars);

    }
}
