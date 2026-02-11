package com.nzhk.wxg.business.wxuser.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nzhk.wxg.business.wxuser.bean.SaveUserInfoReqData;
import com.nzhk.wxg.business.wxuser.bean.UserInfoResData;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginReqData;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginResData;
import com.nzhk.wxg.business.wxuser.entity.WxUser;

public interface IWxUserService extends IService<WxUser> {
    WxUserLoginResData login(WxUserLoginReqData wxUserLoginReqData);

    WxUserLoginResData saveUserInfo(SaveUserInfoReqData data);

    UserInfoResData getUserInfo();
}
