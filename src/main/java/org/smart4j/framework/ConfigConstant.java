package org.smart4j.framework;

/**
 * 提供相关配置项常量
 *
 * @author huangyong
 * @since 1.0.0
 */
public interface ConfigConstant {

    //使用者的配置文件 必须叫这个
    String CONFIG_FILE = "smart.properties";

    //数据库配置的key
    String JDBC_DRIVER = "smart.framework.jdbc.driver";
    String JDBC_URL = "smart.framework.jdbc.url";
    String JDBC_USERNAME = "smart.framework.jdbc.username";
    String JDBC_PASSWORD = "smart.framework.jdbc.password";

    //基础包
    String APP_BASE_PACKAGE = "smart.framework.app.base_package";
    //jsp文件在哪
    String APP_JSP_PATH = "smart.framework.app.jsp_path";
    //静态文件
    String APP_ASSET_PATH = "smart.framework.app.asset_path";
    //todo 上传?
    String APP_UPLOAD_LIMIT = "smart.framework.app.upload_limit";
}
