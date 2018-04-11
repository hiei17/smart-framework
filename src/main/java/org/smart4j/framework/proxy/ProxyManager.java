package org.smart4j.framework.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 代理管理器
 *
 * @author huangyong
 * @since 1.0.0
 */
public class ProxyManager {

    /**
     * @param targetClass 原类
     * @param proxyList   代理list
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(final Class<?> targetClass, final List<Proxy> proxyList) {

        MethodInterceptor methodInterceptor = new MethodInterceptor() {

            /**
             *
             * @param targetObject 原对象
             * @param targetMethod 原方法
             * @param methodParams 原传参
             * @param methodProxy GCLib提供的方法代理对象
             * @return 原方法执行结果
             * @throws Throwable
             */
            @Override
            public Object intercept(Object targetObject, Method targetMethod, Object[] methodParams, MethodProxy methodProxy) throws Throwable {

                ProxyChain proxyChain = new ProxyChain(targetClass, targetObject, targetMethod, methodProxy, methodParams, proxyList);

                Object methodResult = proxyChain.doProxyChain();

                return methodResult;

            }

        };

        //代理targetClass 以后不直接用targetClass了  都实际调用methodInterceptor.intercept
        return (T) Enhancer.create(targetClass, methodInterceptor);

    }
}