package com.nzhk.wxg.business.wxuser.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 微信小程序用户信息实体类
 */
@Data
@Builder
@TableName("wx_user")
public class WxUser {
    
    /**
     * 主键ID
     */
    @TableId
    private String id;
    
    /**
     * 微信小程序用户唯一标识
     */
    private String openid;
    
    /**
     * 微信开放平台统一标识（需绑定开放平台）
     */
    private String unionid;
    
    /**
     * 微信登录会话密钥
     */
    private String sessionKey;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像URL
     */
    private String avatarUrl;
    
    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;
    
    /**
     * 国家
     */
    private String country;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 语言
     */
    private String language;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

}