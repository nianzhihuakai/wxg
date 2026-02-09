package com.nzhk.wxg.common.utils;

import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.Response;

import java.io.PrintWriter;

public class ResponseUtil {

    /**
     * 返回信息
     *
     * @param response
     */
    public static void setResponse(HttpServletResponse response, Integer statusCode, String resultMessage) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = null ;
        try{
            response.setStatus(statusCode);
            String msgStr = JSONObject.toJSONString(resultMessage);
            out = response.getWriter();
            out.append(msgStr);
        }
        catch (Exception e){
            response.setStatus(statusCode);
        }
    }
}
