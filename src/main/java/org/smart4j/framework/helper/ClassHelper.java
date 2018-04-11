package org.smart4j.framework.helper;

import org.smart4j.framework.annotation.Controller;
import org.smart4j.framework.annotation.Service;
import org.smart4j.framework.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 *加载用户配置的包名的路径下所有的类
 * 根据需求遍历筛选返回要的类
 */
public final class ClassHelper {

    //基础包(用户配置的)下所有的类 (没执行过里面的static{}
    private static final Set<Class<?>> CLASS_SET;
    //CLASS_SET准备好
    static {
        //配置文件得到基础包名
        String basePackage = ConfigHelper.getAppBasePackage();
        //基础包下的类全部拿出来
        CLASS_SET = ClassUtil.getClassSet(basePackage);
    }



   //遍历得到 其中所有@Service注解的类
    private static Set<Class<?>> getServiceClassSet() {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        for (Class<?> cls : CLASS_SET) {
            if (cls.isAnnotationPresent(Service.class)) {
                classSet.add(cls);
            }
        }
        return classSet;
    }

   //遍历得到 其中所有@Controller注解的类
    static Set<Class<?>> getControllerClassSet() {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        //遍历基础包下所有的类
        for (Class<?> cls : CLASS_SET) {
            if (cls.isAnnotationPresent(Controller.class)) {
                classSet.add(cls);
            }
        }
        return classSet;
    }

    //以上2个方法都调用 全部返回
     static Set<Class<?>> getBeanClassSet() {
        Set<Class<?>> beanClassSet = new HashSet<Class<?>>();
        beanClassSet.addAll(getServiceClassSet());
        beanClassSet.addAll(getControllerClassSet());
        return beanClassSet;
    }

    //遍历其中每个类 是这个类子类的就返回
     static Set<Class<?>> getClassSetBySuper(Class<?> superClass) {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        //基础包下所有的类遍历 把是这个类型 又不是它本身 的类加进去
        for (Class<?> cls : CLASS_SET) {
            if (superClass.isAssignableFrom(cls) && !superClass.equals(cls)) {
                classSet.add(cls);
            }
        }
        return classSet;
    }

    //遍历其中所有类 有指定注解的返回
     static Set<Class<?>> getClassSetByAnnotation(Class<? extends Annotation> annotationClass) {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        for (Class<?> cls : CLASS_SET) {
            if (cls.isAnnotationPresent(annotationClass)) {
                classSet.add(cls);
            }
        }
        return classSet;
    }

}
