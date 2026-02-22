package com.nzhk.wxg.business.wxuser.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wxg.business.habittype.entity.HabitType;
import com.nzhk.wxg.business.habittype.service.IHabitTypeService;
import com.nzhk.wxg.business.wxuser.bean.SaveUserInfoReqData;
import com.nzhk.wxg.business.wxuser.bean.UserInfoResData;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginReqData;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginResData;
import com.nzhk.wxg.business.wxuser.entity.WxUser;
import com.nzhk.wxg.business.wxuser.vo.WxLoginResVO;
import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.cache.UserInfo;
import com.nzhk.wxg.common.utils.BeanConvertUtil;
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.common.utils.JwtUtil;
import com.nzhk.wxg.mapper.HabitTypeMapper;
import com.nzhk.wxg.mapper.WxUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
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
    @Resource
    private HabitTypeMapper habitTypeMapper;
    @Resource
    private IHabitTypeService habitTypeService;

    @Override
    public WxUserLoginResData login(WxUserLoginReqData wxUserLoginReqData) {
        WxLoginResVO loginResVO = getOpenid(wxUserLoginReqData.getCode());
        String openId = loginResVO.getOpenId();
        String sessionKey = loginResVO.getSessionKey();

        WxUser wxUser = wxUserMapper.selectByOpenId(openId);
        log.info("login openId:{}", openId);
        if (null == wxUser) {
            String userId = wxUserLoginReqData.getUserId();
            log.info("login userId:{}", userId);
            if (StringUtils.isEmpty(userId)) {
                wxUser = WxUser.builder().id(IdUtil.getId()).openid(openId).sessionKey(sessionKey).build();
                wxUserMapper.insert(wxUser);
            } else {
                WxUser wxUserSelect = baseMapper.selectById(userId);
                if (null == wxUserSelect) {
                    wxUser = WxUser.builder().id(IdUtil.getId()).openid(openId).sessionKey(sessionKey).build();
                    log.info("login wxUser:{}", wxUser.getId());
                    wxUserMapper.insert(wxUser);
                } else {
                    wxUser = wxUserSelect;
                    log.info("login wxUserSelect.getId():{}", wxUserSelect.getId());
                    LambdaUpdateWrapper<WxUser> wxUserLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                    wxUserLambdaUpdateWrapper.eq(WxUser::getId, wxUserSelect.getId())
                            .set(WxUser::getOpenid, openId);
                    baseMapper.update(null, wxUserLambdaUpdateWrapper);
                }
            }

        } else {
            log.info("login wxUser.getId():{}", wxUser.getId());
            LambdaUpdateWrapper<WxUser> wxUserLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            wxUserLambdaUpdateWrapper.eq(WxUser::getId, wxUser.getId())
                    .set(WxUser::getSessionKey, sessionKey).set(WxUser::getUpdateTime, LocalDateTime.now());
            wxUserMapper.update(null, wxUserLambdaUpdateWrapper);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", wxUser.getId());
        String token = JwtUtil.generateToken(claims);
        UserInfo userInfo = UserInfo.builder().id(wxUser.getId()).avatarUrl(wxUser.getAvatarUrl()).nickName(wxUser.getNickName()).build();

        LambdaQueryWrapper<HabitType> habitTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitTypeLambdaQueryWrapper.eq(HabitType::getUserId, userInfo.getId());
        List<HabitType> habitTypes = habitTypeMapper.selectList(habitTypeLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(habitTypes)) {
            List<String> typeList = Arrays.asList("生活", "健身", "学习");
            for (int i = 0; i < typeList.size(); i++) {
                String typeName =  typeList.get(i);
                HabitType habitType = new HabitType();
                habitType.setId(IdUtil.getId());
                habitType.setName(typeName);
                habitType.setUserId(userInfo.getId());
                habitType.setSortOrder(i+1);
                habitTypeMapper.insert(habitType);
            }
        }
        return WxUserLoginResData.builder().token(token).userInfo(userInfo).build();
    }

    @Override
    public WxUserLoginResData saveUserInfo(SaveUserInfoReqData data) {
        log.info("saveUserInfo userId:{}, nickName:{}", ContextCache.getUserId(), data != null ? data.getNickName() : null);
        LambdaUpdateWrapper<WxUser> wxUserLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        wxUserLambdaUpdateWrapper.eq(WxUser::getId, ContextCache.getUserId())
                .set(WxUser::getNickName, data.getNickName())
                .set(WxUser::getAvatarUrl, data.getAvatarUrl());
        baseMapper.update(null, wxUserLambdaUpdateWrapper);

        WxUser wxUser = baseMapper.selectById(ContextCache.getUserId());

        UserInfo userInfo = UserInfo.builder().id(wxUser.getId()).avatarUrl(data.getAvatarUrl()).nickName(data.getNickName()).build();
        return WxUserLoginResData.builder().userInfo(userInfo).build();
    }

    @Override
    public UserInfoResData getUserInfo() {
        log.info("getUserInfo userId:{}", ContextCache.getUserId());
        WxUser wxUser = baseMapper.selectById(ContextCache.getUserId());
        return BeanConvertUtil.copySingleProperties(wxUser, UserInfoResData::new);
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
