package com.nzhk.wxg.wechat.service;

import com.alibaba.fastjson.JSONObject;
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.mapper.SubscribeMessageLogMapper;
import com.nzhk.wxg.wechat.config.WechatProperties;
import com.nzhk.wxg.wechat.entity.SubscribeMessageLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信小程序订阅消息发送服务
 * 接口文档: https://developers.weixin.qq.com/miniprogram/dev/server/API/mp-message-management/subscribe-message/api_sendmessage.html
 * <p>
 * 模板字段：thing6=习惯名称，thing15=备注（固定值「微习惯打卡提醒」）
 */
@Slf4j
@Service
public class SubscribeMessageService {

    private static final String SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s";

    @Resource
    private WechatProperties wechatProperties;
    @Resource
    private WechatAccessTokenService wechatAccessTokenService;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private SubscribeMessageLogMapper subscribeMessageLogMapper;

    /**
     * 发送习惯提醒订阅消息
     * 模板：thing6=习惯名称，thing15=备注（微习惯打卡提醒），点击跳转首页打卡列表
     *
     * @param userId    用户表 id，可为 null
     * @param openid    用户 openid
     * @param habitId   习惯ID，可为 null
     * @param habitName 习惯名称
     * @return 是否发送成功
     */
    public boolean sendHabitRemind(String userId, String openid, String habitId, String habitName) {
        String templateId = wechatProperties.getSubscribeTemplateId();
        if (templateId == null || templateId.isEmpty()) {
            log.warn("subscribe template id not configured");
            return false;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing6", Map.of("value", truncateThing(habitName, 20)));
        data.put("thing15", Map.of("value", truncateThing("微习惯打卡提醒", 20)));

        return send(userId, openid, templateId, data, "pages/home/home", "habit_remind", habitId, habitName);
    }

    /**
     * 发送专注结束提醒订阅消息
     * 模板字段沿用现有配置：thing6=习惯名称，thing15=提示语
     */
    public boolean sendFocusFinish(String userId, String openid, String habitId, String habitName) {
        String templateId = wechatProperties.getSubscribeTemplateId();
        if (templateId == null || templateId.isEmpty()) {
            log.warn("subscribe template id not configured");
            return false;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing6", Map.of("value", truncateThing(habitName, 20)));
        data.put("thing15", Map.of("value", truncateThing("专注时间已结束", 20)));
        return send(userId, openid, templateId, data, "pages/home/home", "focus_finish", habitId, habitName);
    }

    private String truncateThing(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen);
    }

    /**
     * 发送订阅消息
     *
     * @param openid     openid
     * @param templateId 模板 id
     * @param data       模板数据，如 {"thing1":{"value":"xxx"},"time2":{"value":"2024-01-01 09:30:00"}}
     * @param page       点击跳转页面，可为 null
     */
    public boolean send(String openid, String templateId, Map<String, Object> data, String page) {
        return send(null, openid, templateId, data, page, null, null, null);
    }

    /**
     * 发送订阅消息并记录日志
     *
     * @param userId    用户表 id，可为 null
     * @param openid    openid
     * @param templateId 模板 id
     * @param data      模板数据
     * @param page      点击跳转页面，可为 null
     * @param bizType   业务类型，可为 null
     * @param bizId     业务ID，可为 null
     * @param extraData 扩展数据，可为 null
     */
    public boolean send(String userId, String openid, String templateId, Map<String, Object> data, String page,
                       String bizType, String bizId, String extraData) {
        String token = wechatAccessTokenService.getAccessToken();
        if (token == null) {
            log.warn("access_token is null, skip send");
            saveLog(userId, openid, templateId, data, bizType, bizId, extraData, 0, 0, "access_token is null");
            return false;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("touser", openid);
        body.put("template_id", templateId);
        body.put("data", data);
        body.put("miniprogram_state", "formal");
        body.put("lang", "zh_CN");
        if (page != null && !page.isEmpty()) {
            body.put("page", page);
        }

        String url = String.format(SEND_URL, token);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(JSONObject.toJSONString(body), headers);

        try {
            String resp = restTemplate.postForObject(url, entity, String.class);
            JSONObject obj = JSONObject.parseObject(resp);
            if (obj != null && obj.getIntValue("errcode") == 0) {
                log.info("subscribe message sent, openid={}, templateId={}", openid, templateId);
                saveLog(userId, openid, templateId, data, bizType, bizId, extraData, 1, null, null);
                return true;
            } else {
                int errcode = obj != null ? obj.getIntValue("errcode") : 0;
                String errmsg = obj != null ? obj.getString("errmsg") : "unknown";
                log.warn("subscribe message send failed: {}", resp);
                saveLog(userId, openid, templateId, data, bizType, bizId, extraData, 0, errcode, errmsg);
                return false;
            }
        } catch (Exception e) {
            log.error("subscribe message send error, openid=" + openid, e);
            saveLog(userId, openid, templateId, data, bizType, bizId, extraData, 0, -1, e.getMessage());
            return false;
        }
    }

    private void saveLog(String userId, String openid, String templateId, Map<String, Object> data,
                        String bizType, String bizId, String extraData,
                        int sendStatus, Integer errcode, String errmsg) {
        try {
            SubscribeMessageLog record = new SubscribeMessageLog();
            record.setId(IdUtil.getId());
            record.setUserId(userId);
            record.setOpenid(openid);
            record.setTemplateId(templateId);
            record.setSendContent(data != null ? JSONObject.toJSONString(data) : null);
            record.setBizType(bizType != null ? bizType : "");
            record.setBizId(bizId);
            record.setExtraData(truncateThing(extraData, 512));
            record.setSendStatus(sendStatus);
            record.setErrcode(errcode);
            record.setErrmsg(truncateThing(errmsg, 256));
            record.setCreatedAt(LocalDateTime.now());
            subscribeMessageLogMapper.insert(record);
        } catch (Exception e) {
            log.error("save subscribe_message_log failed", e);
        }
    }
}
