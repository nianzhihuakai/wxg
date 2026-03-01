package com.nzhk.wxg.common.info;

import lombok.Data;

@Data
public class ResponseInfo<D> {

    private int code;

    private String msg;

    private boolean success;

    private D data;

    public static <T> ResponseInfo<T> success (T data) {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setCode(200);
        responseInfo.setMsg("success");
        responseInfo.setSuccess(true);
        responseInfo.setData(data);
        return responseInfo;
    }

    public static <T> ResponseInfo<T> fail (T data) {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setCode(10000);
        responseInfo.setMsg("fail");
        responseInfo.setSuccess(false);
        responseInfo.setData(data);
        return responseInfo;
    }

    public static <T> ResponseInfo<T> fail (int code, String msg, T data) {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setCode(code);
        responseInfo.setMsg(msg);
        responseInfo.setSuccess(false);
        responseInfo.setData(data);
        return responseInfo;
    }
}
