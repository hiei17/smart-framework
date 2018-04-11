package org.smart4j.framework.bean;

/**
 * 就一个字段的贫血类
 *用于controller方法返回数据 给浏览器
 * @author huangyong
 * @since 1.0.0
 */
public class Data {

    //唯一字段
    private Object model;

    public Data(Object model) {
        this.model = model;
    }

    public Object getModel() {
        return model;
    }
}
