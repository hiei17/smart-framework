package org.smart4j.framework.helper;

import org.smart4j.framework.annotation.Action;
import org.smart4j.framework.bean.Handler;
import org.smart4j.framework.bean.Request;
import org.smart4j.framework.util.ArrayUtil;
import org.smart4j.framework.util.CollectionUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 控制器助手类
 *
 * @author huangyong
 * @since 1.0.0
 */
public final class ControllerHelper {

    //请求(方法和url) ------ handle(负责处理这个请求的Controller类和方法)
    private static final Map<Request, Handler> ACTION_MAP = new HashMap<Request, Handler>();

    static {
        Set<Class<?>> controllerClassSet = ClassHelper.getControllerClassSet();
        if (CollectionUtil.isNotEmpty(controllerClassSet)) {
            for (Class<?> controllerClass : controllerClassSet) {
                oneController(controllerClass);
            }
        }
    }

    private static void oneController(Class<?> controllerClass) {

        Method[] methods = controllerClass.getDeclaredMethods();
        if (ArrayUtil.isEmpty(methods)) {
            return;
        }

        //遍历里面的每个方法
        for (Method method : methods) {
            oneMethod(controllerClass, method);
        }
    }

    private static void oneMethod(Class<?> controllerClass, Method method) {
        //得是有@Action注解的方法
        if (!method.isAnnotationPresent(Action.class)) {
            return;
        }
        //得注解
        Action action = method.getAnnotation(Action.class);

        //..............注解的值 格式对
        String mapping = action.value();
        if (mapping.matches("\\w+:/\\w*")) {
            return;
        }

        String[] array = mapping.split(":");
        if (ArrayUtil.isEmpty(array) && array.length != 2) {
            return;
        }
        //..............注解的值 格式对

        String requestMethod = array[0];
        String requestPath = array[1];

        //todo 整理数据 放好在map里面
        Request request = new Request(requestMethod, requestPath);
        Handler handler = new Handler(controllerClass, method);
        ACTION_MAP.put(request, handler);
    }

    /**
     * @param requestMethod 请求方法
     * @param requestPath   请求url
     * @return 负责处理的controller类和方法
     */
    public static Handler getHandler(String requestMethod, String requestPath) {

        Request request = new Request(requestMethod, requestPath);
        return ACTION_MAP.get(request);
    }
}
