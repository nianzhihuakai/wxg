package com.nzhk.wxg.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.nzhk.wxg.common.info.ResponseInfo;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;

public class ResponseUtil {

    /**
     * 返回简单文本信息（历史兼容）
     */
    public static void setResponse(HttpServletResponse response, Integer statusCode, String resultMessage) {
        writeJsonResponse(response, statusCode, statusCode, resultMessage, null);
    }

    /**
     * 返回统一 JSON 格式：{code, msg, success, data}，供拦截器等场景使用
     */
    public static void writeJsonResponse(HttpServletResponse response, int httpStatus, int code, String msg, Object data) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try {
            response.setStatus(httpStatus);
            ResponseInfo<Object> info = ResponseInfo.fail(code, msg, data);
            String json = JSONObject.toJSONString(info);
            PrintWriter out = response.getWriter();
            out.write(json);
        } catch (Exception e) {
            try {
                response.setStatus(httpStatus);
            } catch (Exception ignored) {
            }
        }
    }
}
