package com.nzhk.wxg.business.wxuser.controller;

import com.nzhk.wxg.business.wxuser.bean.SaveUserInfoReqData;
import com.nzhk.wxg.business.wxuser.bean.UserInfoResData;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginReqData;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginResData;
import com.nzhk.wxg.business.wxuser.service.IWxUserService;
import com.nzhk.wxg.common.exception.BizException;
import com.nzhk.wxg.common.info.RequestInfo;
import com.nzhk.wxg.common.info.ResponseInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
public class WxUserController {

    @Resource
    private IWxUserService wxUserService;

    @PostMapping("login")
    public ResponseInfo login (@RequestBody RequestInfo<WxUserLoginReqData> requestInfo) {
        log.info("login request");
        return ResponseInfo.success(wxUserService.login(requestInfo.getData()));
    }

    @PostMapping("saveUserInfo")
    public ResponseInfo<WxUserLoginResData> saveUserInfo (@RequestBody RequestInfo<SaveUserInfoReqData> requestInfo) {
        log.info("saveUserInfo request, nickName:{}", requestInfo.getData() != null ? requestInfo.getData().getNickName() : null);
        return ResponseInfo.success(wxUserService.saveUserInfo(requestInfo.getData()));
    }

    @PostMapping("getUserInfo")
    public ResponseInfo<UserInfoResData> getUserInfo () {
        log.info("getUserInfo request");
        try {
            return ResponseInfo.success(wxUserService.getUserInfo());
        } catch (BizException e) {
            return ResponseInfo.fail(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.error("getUserInfo error", e);
            return ResponseInfo.fail(50000, "服务器异常", null);
        }
    }
}
