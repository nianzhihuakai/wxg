package com.nzhk.wxg.wechat.service;

import com.alibaba.fastjson.JSONObject;
import com.nzhk.wxg.wechat.config.WechatProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    /**
     * 发送习惯提醒订阅消息
     * 模板：thing6=习惯名称，thing15=备注（微习惯打卡提醒），点击跳转首页打卡列表
     *
     * @param openid    用户 openid
     * @param habitName 习惯名称
     * @return 是否发送成功
     */
    public boolean sendHabitRemind(String openid, String habitName) {
        String templateId = wechatProperties.getSubscribeTemplateId();
        if (templateId == null || templateId.isEmpty()) {
            log.warn("subscribe template id not configured");
            return false;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing6", Map.of("value", truncateThing(habitName, 20)));
        data.put("thing15", Map.of("value", truncateThing("微习惯打卡提醒", 20)));

        return send(openid, templateId, data, "pages/home/home");
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
        String token = wechatAccessTokenService.getAccessToken();
        if (token == null) {
            log.warn("access_token is null, skip send");
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
                return true;
            } else {
                log.warn("subscribe message send failed: {}", resp);
                return false;
            }
        } catch (Exception e) {
            log.error("subscribe message send error, openid=" + openid, e);
            return false;
        }
    }
}
