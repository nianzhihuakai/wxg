package com.nzhk.wxg.business.wxuser.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginReqData;
import com.nzhk.wxg.business.wxuser.entity.WxUser;
import com.nzhk.wxg.mapper.WxUserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class WxUserServiceImpl extends ServiceImpl<WxUserMapper, WxUser> implements IWxUserService {

    @Resource
    private WxUserMapper wxUserMapper;
    @Resource
    private RestTemplate restTemplate;

    @Override
    public void login(WxUserLoginReqData wxUserLoginReqData) {
        WxUser wxUser = new WxUser();
        wxUser.setId(UUID.randomUUID().toString());
        wxUser.setOpenid("123");
        wxUserMapper.insert(wxUser);
    }
}
