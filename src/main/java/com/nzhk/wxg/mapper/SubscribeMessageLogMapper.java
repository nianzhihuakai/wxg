package com.nzhk.wxg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nzhk.wxg.wechat.entity.SubscribeMessageLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订阅消息发送记录 Mapper 接口
 */
@Mapper
public interface SubscribeMessageLogMapper extends BaseMapper<SubscribeMessageLog> {
}
