package org.smart4j.framework.helper;

import org.smart4j.framework.bean.FormParam;
import org.smart4j.framework.bean.Param;
import org.smart4j.framework.util.ArrayUtil;
import org.smart4j.framework.util.CodecUtil;
import org.smart4j.framework.util.StreamUtil;
import org.smart4j.framework.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 请求助手类
 *把请求解析出传参 放入Param
 * @author huangyong
 * @since 1.0.0
 */
public final class RequestHelper {

    /**
     * 创建请求对象
     */
    public static Param createParam(HttpServletRequest request) throws IOException {
        List<FormParam> formParamList = new ArrayList<FormParam>();
        formParamList.addAll(parseFormParams(request));
        formParamList.addAll(parseUrlParams(request));
        return new Param(formParamList);
    }

    //从请求中拿出 表格post数据
    private static List<FormParam> parseFormParams(HttpServletRequest request) {
        List<FormParam> formParamList = new ArrayList<FormParam>();
        Enumeration<String> paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()) {

            String fieldName = paramNames.nextElement();

            String[] fieldValues = request.getParameterValues(fieldName);
            if (ArrayUtil.isEmpty(fieldValues)) {
                continue;
            }

            Object fieldValue;
            if (fieldValues.length == 1) {
                fieldValue = fieldValues[0];
            } else {
                //多个的 转字符串
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i < fieldValues.length; i++) {
                    sb.append(fieldValues[i]);
                    if (i != fieldValues.length - 1) {
                        sb.append(StringUtil.SEPARATOR);
                    }
                }
                fieldValue = sb.toString();
            }

            formParamList.add(new FormParam(fieldName, fieldValue));
        }
        return formParamList;
    }

    //url的传参
    private static List<FormParam> parseUrlParams(HttpServletRequest request) throws IOException {

        List<FormParam> formParamList = new ArrayList<FormParam>();
        //输入流转String
        String string = StreamUtil.getString(request.getInputStream());
        ////utf-8解码   可传输→可读
        String body = CodecUtil.decodeURL(string);

        if (StringUtil.isEmpty(body)) {
            return formParamList;
        }

        String[] kvs = StringUtil.splitString(body, "&");
        if (ArrayUtil.isEmpty(kvs)) {
            return formParamList;
        }
        for (String kv : kvs) {
            String[] array = StringUtil.splitString(kv, "=");
            if (ArrayUtil.isNotEmpty(array) && array.length == 2) {
                String fieldName = array[0];
                String fieldValue = array[1];
                formParamList.add(new FormParam(fieldName, fieldValue));
            }
        }
        return formParamList;
    }



}
