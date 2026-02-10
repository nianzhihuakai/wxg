package com.nzhk.wxg.business.wxuser.controller;

import com.nzhk.wxg.business.wxuser.bean.SaveUserInfoReqData;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginReqData;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginResData;
import com.nzhk.wxg.business.wxuser.service.IWxUserService;
import com.nzhk.wxg.common.info.RequestInfo;
import com.nzhk.wxg.common.info.ResponseInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class WxUserController {

    @Resource
    private IWxUserService wxUserService;

    @PostMapping("login")
    public ResponseInfo login (@RequestBody RequestInfo<WxUserLoginReqData> requestInfo) {
        return ResponseInfo.success(wxUserService.login(requestInfo.getData()));
    }

    @PostMapping("saveUserInfo")
    public ResponseInfo<WxUserLoginResData> saveUserInfo (@RequestBody RequestInfo<SaveUserInfoReqData> requestInfo) {
        return ResponseInfo.success(wxUserService.saveUserInfo(requestInfo.getData()));
    }
}
