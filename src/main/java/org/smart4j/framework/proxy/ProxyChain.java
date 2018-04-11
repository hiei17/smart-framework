package org.smart4j.framework.proxy;

import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 代理链
 *
 * @author huangyong
 * @since 1.0.0
 */
public class ProxyChain {

    private final Class<?> targetClass;
    private final Object targetObject;//原对象
    private final Method targetMethod;

    private final MethodProxy methodProxy;//CGLib提供的方法代理对象
    private final Object[] methodParams;//原传参

    //代理列表
    private List<Proxy> proxyList = new ArrayList<Proxy>();
    //代理对象的计数器
    private int proxyIndex = 0;

     ProxyChain(Class<?> targetClass, Object targetObject, Method targetMethod, MethodProxy methodProxy, Object[] methodParams, List<Proxy> proxyList) {
        this.targetClass = targetClass;
        this.targetObject = targetObject;
        this.targetMethod = targetMethod;
        this.methodProxy = methodProxy;
        this.methodParams = methodParams;
        this.proxyList = proxyList;
    }

     Object[] getMethodParams() {
        return methodParams;
    }

     Class<?> getTargetClass() {
        return targetClass;
    }

     Method getTargetMethod() {
        return targetMethod;
    }

     Object doProxyChain() throws Throwable {
        Object methodResult;
        //执行完代理们 才执行原方法
        if (proxyIndex < proxyList.size()) {
            //代理
            methodResult = proxyList.get(proxyIndex++).doProxy(this);
        } else {
            //原方法执行
            methodResult = methodProxy.invokeSuper(targetObject, methodParams);
        }
        return methodResult;
    }
}