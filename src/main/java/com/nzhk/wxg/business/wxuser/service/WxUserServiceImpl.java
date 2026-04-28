package com.nzhk.wxg.business.wxuser.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wxg.business.habit.entity.Habit;
import com.nzhk.wxg.business.habittype.entity.HabitType;
import com.nzhk.wxg.business.wxuser.bean.SaveUserInfoReqData;
import com.nzhk.wxg.business.wxuser.bean.UserInfoResData;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginReqData;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginResData;
import com.nzhk.wxg.business.wxuser.entity.WxUser;
import com.nzhk.wxg.business.wxuser.vo.WxLoginResVO;
import com.nzhk.wxg.common.exception.BizException;
import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.cache.UserInfo;
import com.nzhk.wxg.common.utils.BeanConvertUtil;
import com.nzhk.wxg.common.utils.FileSignUtil;
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.common.utils.JwtUtil;
import com.nzhk.wxg.mapper.HabitMapper;
import com.nzhk.wxg.mapper.HabitTypeMapper;
import com.nzhk.wxg.mapper.WxUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
@Slf4j
@Service
public class WxUserServiceImpl extends ServiceImpl<WxUserMapper, WxUser> implements IWxUserService {
    private static final String DEFAULT_NICKNAME_PREFIX = "微习惯_";
    private static final String NICKNAME_RANDOM_CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int NICKNAME_RANDOM_LENGTH = 4;
    private static final String DEFAULT_HABIT_NAME = "每日自律打卡";
    private static final String DEFAULT_LIFE_TYPE_NAME = "生活";

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
    private HabitMapper habitMapper;

    @Resource
    private FileSignUtil fileSignUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxUserLoginResData login(WxUserLoginReqData wxUserLoginReqData) {
        WxLoginResVO loginResVO = getOpenid(wxUserLoginReqData.getCode());
        String openId = loginResVO.getOpenId();
        String sessionKey = loginResVO.getSessionKey();

        WxUser wxUser = wxUserMapper.selectByOpenId(openId);
        log.info("login openId:{}", openId);
        boolean isNewUser = false;
        if (null == wxUser) {
            String userId = wxUserLoginReqData.getUserId();
            log.info("login userId:{}", userId);
            if (StringUtils.isEmpty(userId)) {
                wxUser = WxUser.builder().id(IdUtil.getId()).openid(openId).sessionKey(sessionKey).nickName(generateDefaultNickName()).build();
                wxUserMapper.insert(wxUser);
                isNewUser = true;
            } else {
                WxUser wxUserSelect = baseMapper.selectById(userId);
                if (null == wxUserSelect) {
                    wxUser = WxUser.builder().id(IdUtil.getId()).openid(openId).sessionKey(sessionKey).nickName(generateDefaultNickName()).build();
                    log.info("login wxUser:{}", wxUser.getId());
                    wxUserMapper.insert(wxUser);
                    isNewUser = true;
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
        wxUser = ensureUserNickName(wxUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", wxUser.getId());
        String token = JwtUtil.generateToken(claims);
        String avatarUrl = fileSignUtil.signFileUrlIfNeeded(wxUser.getAvatarUrl());
        UserInfo userInfo = UserInfo.builder().id(wxUser.getId()).avatarUrl(avatarUrl).nickName(wxUser.getNickName()).build();

        if (isNewUser) {
            HabitType lifeType = initDefaultHabitTypes(userInfo.getId());
            createDefaultHabit(userInfo.getId(), lifeType.getId());
        }
        return WxUserLoginResData.builder().token(token).userInfo(userInfo).build();
    }

    @Override
    public WxUserLoginResData saveUserInfo(SaveUserInfoReqData data) {
        log.info("saveUserInfo userId:{}, nickName:{}", ContextCache.getUserId(), data != null ? data.getNickName() : null);
        String storedAvatarUrl = fileSignUtil.toStoredUrl(data != null ? data.getAvatarUrl() : null);
        String nickName = StringUtils.trimToEmpty(data != null ? data.getNickName() : null);
        if (StringUtils.isBlank(nickName)) {
            nickName = generateDefaultNickName();
        }
        LambdaUpdateWrapper<WxUser> wxUserLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        wxUserLambdaUpdateWrapper.eq(WxUser::getId, ContextCache.getUserId())
                .set(WxUser::getNickName, nickName)
                .set(WxUser::getAvatarUrl, storedAvatarUrl);
        baseMapper.update(null, wxUserLambdaUpdateWrapper);

        WxUser wxUser = baseMapper.selectById(ContextCache.getUserId());
        wxUser = ensureUserNickName(wxUser);

        String avatarUrl = fileSignUtil.signFileUrlIfNeeded(storedAvatarUrl);
        UserInfo userInfo = UserInfo.builder().id(wxUser.getId()).avatarUrl(avatarUrl).nickName(wxUser.getNickName()).build();
        return WxUserLoginResData.builder().userInfo(userInfo).build();
    }

    @Override
    public UserInfoResData getUserInfo() {
        log.info("getUserInfo userId:{}", ContextCache.getUserId());
        WxUser wxUser = baseMapper.selectById(ContextCache.getUserId());
        if (wxUser == null) {
            throw new BizException(40400, "用户不存在");
        }
        wxUser = ensureUserNickName(wxUser);
        UserInfoResData res = BeanConvertUtil.copySingleProperties(wxUser, UserInfoResData::new);
        if (res != null && res.getAvatarUrl() != null) {
            res.setAvatarUrl(fileSignUtil.signFileUrlIfNeeded(res.getAvatarUrl()));
        }
        return res;
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

    private WxUser ensureUserNickName(WxUser wxUser) {
        if (wxUser == null || StringUtils.isNotBlank(wxUser.getNickName())) {
            return wxUser;
        }
        String nickName = generateDefaultNickName();
        LambdaUpdateWrapper<WxUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WxUser::getId, wxUser.getId())
                .set(WxUser::getNickName, nickName);
        baseMapper.update(null, updateWrapper);
        wxUser.setNickName(nickName);
        return wxUser;
    }

    private String generateDefaultNickName() {
        StringBuilder code = new StringBuilder(NICKNAME_RANDOM_LENGTH);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < NICKNAME_RANDOM_LENGTH; i++) {
            code.append(NICKNAME_RANDOM_CHARS.charAt(random.nextInt(NICKNAME_RANDOM_CHARS.length())));
        }
        return DEFAULT_NICKNAME_PREFIX + code;
    }

    private HabitType initDefaultHabitTypes(String userId) {
        List<String> typeList = Arrays.asList("生活", "健身", "学习");
        HabitType lifeType = null;
        for (int i = 0; i < typeList.size(); i++) {
            String typeName = typeList.get(i);
            HabitType habitType = new HabitType();
            habitType.setId(IdUtil.getId());
            habitType.setName(typeName);
            habitType.setUserId(userId);
            habitType.setSortOrder(i + 1);
            habitTypeMapper.insert(habitType);
            if (DEFAULT_LIFE_TYPE_NAME.equals(typeName)) {
                lifeType = habitType;
            }
        }
        if (lifeType == null) {
            throw new BizException(50000, "默认分类初始化失败");
        }
        return lifeType;
    }

    private void createDefaultHabit(String userId, String lifeHabitTypeId) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(21);
        Habit habit = new Habit();
        habit.setId(IdUtil.getId());
        habit.setUserId(userId);
        habit.setHabitTypeId(lifeHabitTypeId);
        habit.setName(DEFAULT_HABIT_NAME);
        habit.setStartDate(startDate);
        habit.setEndDate(endDate);
        habit.setRemindFlag(false);
        habit.setRemindTime(null);
        habit.setCheckInFrequencyType("fixed");
        habit.setCheckInFrequency("1,2,3,4,5,6,7");
        habit.setPromptCheckinReflection(false);
        habit.setStreakDays(0);
        habit.setMaxStreakDays(0);
        habitMapper.insert(habit);
    }
}
