package org.smart4j.framework;

import org.smart4j.framework.bean.Data;
import org.smart4j.framework.bean.Handler;
import org.smart4j.framework.bean.Param;
import org.smart4j.framework.bean.View;
import org.smart4j.framework.helper.*;
//工具类
import org.smart4j.framework.util.JsonUtil;
import org.smart4j.framework.util.ReflectionUtil;
import org.smart4j.framework.util.StringUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 请求转发器 一切的起点
 *
 *  MVC的核心
 */

//拦截所有请求
@WebServlet(urlPatterns = "/*", loadOnStartup = 0)//就通过这个注解起作用
public class DispatcherServlet extends HttpServlet {

    //由tomcat调用 就开始时调用一次
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        //准备工作 放好那些bean 放好 请求-处理方法放的map
        HelperLoader.init();

        ServletContext servletContext = servletConfig.getServletContext();

        //注册jsp文件和静态资源 (通过原有的方法
        registerServlet(servletContext);

        //todo
        UploadHelper.init(servletContext);
    }

    private void registerServlet(ServletContext servletContext) {
        //servlet环境里面得jsp注册器
        ServletRegistration jspServlet = servletContext.getServletRegistration("jsp");
        jspServlet.addMapping("/index.jsp");
        jspServlet.addMapping(ConfigHelper.getAppJspPath() + "*");//注册配置路径下 所有jsp文件

        //静态资源注册
        ServletRegistration defaultServlet = servletContext.getServletRegistration("default");
        defaultServlet.addMapping("/favicon.ico");//网站图标 一定叫这个
        defaultServlet.addMapping(ConfigHelper.getAppAssetPath() + "*");//用户指定的路径下的文件
    }

    /**
     *每次接到请求 都会到这处理
     * @param request 请求
     * @param response 响应
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //保管本线程的request, response 方便本线程任何地方使用
        ServletHelper.init(request, response);

        try {
            //给request返回负责接待的类和方法
            Handler handler = getHandler(request);

            if (handler == null) {
                return;
            }

            //从request解析出传参
            Param param = parseParam(request);

            //执行controller那个方法
            Object result = invokeHandler(handler, param);

            //去页面
            if (result instanceof View) {
                handleViewResult((View) result, request, response);
            }

            //json对象
            if (result instanceof Data) {
                handleJsonResult((Data) result, response);
            }

        } finally {
            //没用了
            //摧毁本线程保存的request, response
            ServletHelper.destroy();
        }
    }

    private Object invokeHandler(Handler handler, Param param) {
        Object result;

        Class<?> controllerClass = handler.getControllerClass();
        //得到接待的controller对象
        Object controllerBean = BeanHelper.getBean(controllerClass);
        Method actionMethod = handler.getActionMethod();//负责接待的方法
        //反射运行得到结果
        if (param.isEmpty()) {
            result = ReflectionUtil.invokeMethod(controllerBean, actionMethod);
        } else {
            result = ReflectionUtil.invokeMethod(controllerBean, actionMethod, param);
        }
        return result;
    }

    private Param parseParam(HttpServletRequest request) throws IOException {
        Param param;
        if (UploadHelper.isMultipart(request)) {
            param = UploadHelper.createParam(request);
        } else {
            //从请求中解析出参数
            param = RequestHelper.createParam(request);
        }
        return param;
    }

    private Handler getHandler(HttpServletRequest request) {
        //get/post
        String requestMethod = request.getMethod().toLowerCase();
        //请求url
        String requestPath = request.getPathInfo();

        return ControllerHelper.getHandler(requestMethod, requestPath);
    }

    /**
     *
     * @param view 指定的视图
     * @param request 请求:能得到项目根路径 和带参数
     * @param response 响应 : 是在用户的浏览器端工作 让浏览器请求另一个url 302
     * @throws IOException
     * @throws ServletException
     */
    private void handleViewResult(View view, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = view.getPath();
        if (StringUtil.isEmpty(path)) {
            return;
        }

        //浏览器重定向
        if (path.startsWith("/")) {
            response.sendRedirect(request.getContextPath() + path);
            return;
        }

        //在服务器的跳转
        Map<String, Object> model = view.getModel();
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
        //从客户端接收请求并将其发送到服务器上的任何资源（例如servlet，HTML文件或JSP文件）的对象
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(ConfigHelper.getAppJspPath() + path);
        requestDispatcher.forward(request, response);//导向jsp
    }

    /**
     * 返回json
     * @param data 准备转json的返回对象
     * @param response 用来得到输出流 最终输出json
     * @throws IOException
     */
    private void handleJsonResult(Data data, HttpServletResponse response) throws IOException {

        Object model = data.getModel();
        if (model == null) {
            return;

        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        String json = JsonUtil.toJson(model);//todo 自己实现的 调用jackson的方法
        writer.write(json);
        writer.flush();
        writer.close();
    }



}
