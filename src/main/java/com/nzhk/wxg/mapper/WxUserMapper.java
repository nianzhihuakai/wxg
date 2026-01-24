package com.nzhk.wxg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nzhk.wxg.business.wxuser.entity.WxUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WxUserMapper extends BaseMapper<WxUser> {
    @Select("select * from wx_user where openid = #{openId}")
    WxUser selectByOpenId(@Param("openId") String openId);
}
