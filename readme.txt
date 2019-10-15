这是自己搭建的简易版的Spring。
实现了简单的Spring IOC容器初始化，以及容器中bean的实例化和依赖注入
模仿Spring的流程在Servlet初始化init()的时候，实现了
1.定位：
    通过加载application.properties的信息，找到要加载对象的包文件com.xiaoden.demo
2.加载：
    通过定位找到要初始化的包目录，找到该目录下的每个文件对象，把对象包名记录到
classNames中。
3.注册：
    通过反射把有Controller和Service注解的全包名对象实例化放到简易版ioc容器
beanMap中。
4.依赖注入：通过反射，把ioc容器中的bean对象中有@Autowire注解的对象注入到该bean
中，实现依赖注入。

测试在DispatchServlet类67行中上面步骤之后调用容器中对象的方法后

