package org.smart4j.framework.helper;

import org.smart4j.framework.annotation.Inject;
import org.smart4j.framework.util.ArrayUtil;
import org.smart4j.framework.util.CollectionUtil;
import org.smart4j.framework.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 依赖注入助手类
 *遍历所有管理的bean  看看里面有没有需要注入的注解 要是有就setField
 * @author huangyong
 * @since 1.0.0
 */
public final class IocHelper {

    //一切在这里就做完了 就是本类被加载进虚拟机时候 就运行了
    static {
        //基础包下所有@Service @Controller注解的 map里面  class----实例
        Map<Class<?>, Object> beanMap = BeanHelper.getBeanMap();

        if (CollectionUtil.isEmpty(beanMap)) {
            //遍历每个bean 看它们有没有需要注入的
            for (Map.Entry<Class<?>, Object> beanEntry : beanMap.entrySet()) {

                injectOneBean(beanMap, beanEntry);
            }

        }
    }

    /**
     *
     * @param beanMap 所有bean
     * @param beanEntry 现在要处理的实例
     */
    private static void injectOneBean(Map<Class<?>, Object> beanMap, Map.Entry<Class<?>, Object> beanEntry) {

        Class<?> beanClass = beanEntry.getKey();
        Object beanInstance = beanEntry.getValue();

        //类字段
        Field[] beanFields = beanClass.getDeclaredFields();

        if (ArrayUtil.isEmpty(beanFields)) {
           return;
        }
        //遍历每个field
        for (Field beanField : beanFields) {
            //有注入注解
            if (beanField.isAnnotationPresent(Inject.class)) {
                //注入(setj进去
                injectOneField(beanMap, beanInstance, beanField);
            }
        }
    }

    /**
     *
     * @param beanMap 所有容器里面bean
     * @param beanInstance 需要字段注入的实例
     * @param beanField 字段
     */
    private static void injectOneField(Map<Class<?>, Object> beanMap, Object beanInstance, Field beanField) {
        //字段类型
        Class<?> beanFieldClass = beanField.getType();
        //被注入的
        Object beanFieldInstance = beanMap.get(beanFieldClass);

        //反射set进需要注入的实例里面
        if (beanFieldInstance != null) {
            ReflectionUtil.setField(beanInstance, beanField, beanFieldInstance);
        }
    }
}
