package com.nzhk.wxg.business.wxuser.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wxg.business.wxuser.entity.WxUser;
import com.nzhk.wxg.mapper.WxUserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WxUserServiceImpl extends ServiceImpl<WxUserMapper, WxUser> implements IWxUserService {

    @Resource
    private WxUserMapper wxUserMapper;

    @Override
    public void login() {
        WxUser wxUser = new WxUser();
        wxUser.setId(UUID.randomUUID().toString());
        wxUser.setOpenid("123");
        wxUserMapper.insert(wxUser);
    }
}
