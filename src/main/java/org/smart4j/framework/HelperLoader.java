package org.smart4j.framework;

import org.smart4j.framework.helper.*;
import org.smart4j.framework.util.ClassUtil;

/**
 *为了运行里面的static{}
 * 把容易里面的bean都放好了
 *
 * @author huangyong
 * @since 1.0.0
 */
final class HelperLoader {

    //HttpServlet的init中调用
     static void init() {
        //这个顺利不能乱
        Class<?>[] classList = {
            ClassHelper.class,//基础包(配置文件指定的)下所有的Class 放在里面一个set
            BeanHelper.class,//(遍历ClassHelper里面得到的set)基础包下所有@Service @Controller注解的实例  map里面  class----实例 放好
            AopHelper.class,// 切面 把BeanHelper里面map里面的实例需要换的用代理类换掉

            IocHelper.class, //所有ioc组装 检查BeanHelper每个bean 看有没有field有注入注解的 有就set进去(用field声明的class去BeanHelper里面的map拿实例
            ControllerHelper.class//请求(方法和url) ------ handle(负责处理这个请求的Controller类和方法) 遍历BeanHelper里面的controller实例就处理完了
        };

        //统一加载这些类 一加载里面的static{}立马就运行了
        for (Class<?> cls : classList) {
            //加载类(要执行静态代码块)
            ClassUtil.loadClass(cls.getName(),true);
        }
    }
}