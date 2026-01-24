package com.nzhk.wxg.business.wxuser.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginReqData;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginResData;
import com.nzhk.wxg.business.wxuser.entity.WxUser;
import com.nzhk.wxg.business.wxuser.vo.WxLoginResVO;
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.mapper.WxUserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WxUserServiceImpl extends ServiceImpl<WxUserMapper, WxUser> implements IWxUserService {

    String appid = "wxa46f1050fff7f28b";
    String secret = "275745dae4dda4a21b04497d7a3cdc9b";
    // 登录接口
    private String loginUrl = "https://api.weixin.qq.com/sns/jscode2session";
    @Resource
    private WxUserMapper wxUserMapper;
    @Resource
    private RestTemplate restTemplate;

    @Override
    public WxUserLoginResData login(WxUserLoginReqData wxUserLoginReqData) {
        WxLoginResVO loginResVO = getOpenid(wxUserLoginReqData.getCode());
        String openId = loginResVO.getOpenId();
        String sessionKey = loginResVO.getSessionKey();

        WxUser wxUser = wxUserMapper.selectByOpenId(openId);
        if (null == wxUser) {
            wxUser = WxUser.builder().id(IdUtil.getId()).openid(openId).sessionKey(sessionKey).build();
            wxUserMapper.insert(wxUser);
        } else {
            LambdaUpdateWrapper<WxUser> wxUserLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            wxUserLambdaUpdateWrapper.eq(WxUser::getId, wxUser.getId())
                    .set(WxUser::getSessionKey, sessionKey).set(WxUser::getUpdateTime, LocalDateTime.now());
            wxUserMapper.update(null, wxUserLambdaUpdateWrapper);
        }
        return WxUserLoginResData.builder().id(wxUser.getId()).build();
    }


    private WxLoginResVO getOpenid(String code){
        //调用微信接口服务，获得当前微信用户的openId
        Map<String,String> map = new HashMap<>();
        map.put("appid",appid);
        map.put("secret",secret);
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={2}&grant_type=authorization_code";
        String replaceUrl = url.replace("{0}", appid).replace("{1}", secret).replace("{2}", code);
        String json = restTemplate.getForObject(replaceUrl, String.class);
        JSONObject jsonObject = JSONObject.parseObject(json);
        WxLoginResVO loginResVO = null;
        if (jsonObject != null) {
            loginResVO = WxLoginResVO.builder().openId(jsonObject.getString("openid")).sessionKey(jsonObject.getString("session_key")).build();
        }
        return loginResVO;
    }
}
