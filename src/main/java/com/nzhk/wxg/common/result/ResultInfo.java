package com.nzhk.wxg.common.result;

import lombok.Builder;
import lombok.Data;

@Data
public class ResultInfo {

    private int code;

    private boolean success;

    private Object data;

    public static ResultInfo success (Object data) {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setCode(200);
        resultInfo.setSuccess(true);
        resultInfo.setData(data);
        return resultInfo;
    }

    public static ResultInfo fail (Object data) {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setCode(10000);
        resultInfo.setSuccess(false);
        resultInfo.setData(data);
        return resultInfo;
    }
}
