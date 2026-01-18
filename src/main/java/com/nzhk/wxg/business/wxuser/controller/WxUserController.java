package com.nzhk.wxg.business.wxuser.controller;

import com.nzhk.wxg.business.wxuser.bean.WxUserLoginReqData;
import com.nzhk.wxg.business.wxuser.service.IWxUserService;
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
    public String login (@RequestBody WxUserLoginReqData wxUserLoginReqData) {
        wxUserService.login(wxUserLoginReqData);
        return "234";
    }
}
