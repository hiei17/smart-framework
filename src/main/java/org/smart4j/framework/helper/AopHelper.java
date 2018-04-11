package org.smart4j.framework.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.annotation.Aspect;
import org.smart4j.framework.annotation.Service;
import org.smart4j.framework.proxy.AspectProxy;
import org.smart4j.framework.proxy.Proxy;
import org.smart4j.framework.proxy.ProxyManager;
import org.smart4j.framework.proxy.TransactionProxy;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * 方法拦截助手类
 *
 * @author huangyong
 * @since 1.0.0
 */
public final class AopHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AopHelper.class);

    static {
        try {
            //切面类---对应所有目标类 组合
            Map<Class<?>, Set<Class<?>>> proxyMap = createProxyMap();
            //目标类----相关切面类
            Map<Class<?>, List<Proxy>> targetMap = createTargetMap(proxyMap);

            for (Map.Entry<Class<?>, List<Proxy>> targetEntry : targetMap.entrySet()) {
                //目标类
                Class<?> targetClass = targetEntry.getKey();
                //所有和它相关的切面
                List<Proxy> proxyList = targetEntry.getValue();
                //生成代理
                Object proxy = ProxyManager.createProxy(targetClass, proxyList);
                //把bean容器里面这个目标类换成代理类
                BeanHelper.setBean(targetClass, proxy);
            }

        } catch (Exception e) {
            LOGGER.error("aop failure", e);
        }
    }

    private static Map<Class<?>, Set<Class<?>>> createProxyMap() throws Exception {
        Map<Class<?>, Set<Class<?>>> proxyMap = new HashMap<Class<?>, Set<Class<?>>>();
        //放入基础包下所有能找到的 切面类---模板类 组合
        addAspectProxy(proxyMap);

        //事务
        addTransactionProxy(proxyMap);
        return proxyMap;
    }

    private static void addAspectProxy(Map<Class<?>, Set<Class<?>>> proxyMap) throws Exception {
        //得到AspectProxy的所有子类
        Set<Class<?>> proxyClassSet = ClassHelper.getClassSetBySuper(AspectProxy.class);

        for (Class<?> proxyClass : proxyClassSet) {
            //加上@Aspect注解才生效
            if (proxyClass.isAnnotationPresent(Aspect.class)) {

                Aspect aspect = proxyClass.getAnnotation(Aspect.class);
                //被切的类
                Set<Class<?>> targetClassSet = createTargetClassSet(aspect);
                proxyMap.put(proxyClass, targetClassSet);
            }

        }

    }

    private static void addTransactionProxy(Map<Class<?>, Set<Class<?>>> proxyMap) {
        //找到@service类
        Set<Class<?>> serviceClassSet = ClassHelper.getClassSetByAnnotation(Service.class);
        //都事务
        proxyMap.put(TransactionProxy.class, serviceClassSet);
    }

    /**
     *
     * @param aspect 如@Aspect(Controller.class)
     * @return 返回被切的类 如有@Controller的类
     * @throws Exception
     */
    private static Set<Class<?>> createTargetClassSet(Aspect aspect) throws Exception {
        Set<Class<?>> targetClassSet = new HashSet<Class<?>>();
        //要切的注解
        Class<? extends Annotation> annotation = aspect.value();
        //有这个注解的类
        Set<Class<?>> classSet = ClassHelper.getClassSetByAnnotation(annotation);
        targetClassSet.addAll(classSet);

        return targetClassSet;
    }

    private static Map<Class<?>, List<Proxy>> createTargetMap(Map<Class<?>, Set<Class<?>>> proxyMap) throws Exception {
        Map<Class<?>, List<Proxy>> targetMap = new HashMap<Class<?>, List<Proxy>>();
        for (Map.Entry<Class<?>, Set<Class<?>>> proxyEntry : proxyMap.entrySet()) {
            //切
            Class<?> proxyClass = proxyEntry.getKey();
            //目标类
            Set<Class<?>> targetClassSet = proxyEntry.getValue();

            for (Class<?> targetClass : targetClassSet) {

                Proxy proxy = (Proxy) proxyClass.newInstance();
                if (targetMap.containsKey(targetClass)) {
                    targetMap.get(targetClass).add(proxy);
                } else {
                    List<Proxy> proxyList = new ArrayList<Proxy>();
                    proxyList.add(proxy);
                    targetMap.put(targetClass, proxyList);
                }
            }

        }
        return targetMap;
    }
}
