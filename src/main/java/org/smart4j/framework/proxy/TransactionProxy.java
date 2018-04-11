package org.smart4j.framework.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.annotation.Transaction;
import org.smart4j.framework.helper.DatabaseHelper;

import java.lang.reflect.Method;

/**
 * 事务代理
 *
 * @author huangyong
 * @since 1.0.0
 */
public class TransactionProxy implements Proxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionProxy.class);

    //保证一个线程 这个逻辑只执行一次  默认值是false没执行
    private static final ThreadLocal<Boolean> FLAG_HOLDER = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    @Override
    public Object doProxy(ProxyChain proxyChain) throws Throwable {
        Object result;
        boolean flag = FLAG_HOLDER.get();
        //被切的目标方法
        Method method = proxyChain.getTargetMethod();
        //没被代理过 并且 有注解@Transaction
        if (!flag && method.isAnnotationPresent(Transaction.class)) {

            FLAG_HOLDER.set(true);

            try {
                //本线程的数据库连接: 关闭自动提交
                DatabaseHelper.beginTransaction();
                LOGGER.debug("begin transaction");

                //放行(执行sql
                result = proxyChain.doProxyChain();

                //本线程的数据库连接: 提交 关闭连接
                DatabaseHelper.commitTransaction();
                LOGGER.debug("commit transaction");
            } catch (Exception e) {
                DatabaseHelper.rollbackTransaction();
                LOGGER.debug("rollback transaction");
                throw e;
            } finally {
                FLAG_HOLDER.remove();
            }
        } else {
            result = proxyChain.doProxyChain();
        }
        return result;
    }
}