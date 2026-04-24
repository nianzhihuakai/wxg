package com.nzhk.wxg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nzhk.wxg.business.habitcheckin.bean.UserCheckInAggregateItem;
import com.nzhk.wxg.business.habitcheckin.bean.UserCheckInDateItem;
import com.nzhk.wxg.business.habitcheckin.bean.UserCheckInRankItemResData;
import com.nzhk.wxg.business.habitcheckin.entity.HabitCheckIn;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * <p>
 * 习惯打卡记录表 Mapper 接口
 * </p>
 *
 * @author lxy
 * @since 2026-02-06
 */
public interface HabitCheckInMapper extends BaseMapper<HabitCheckIn> {

    @Select("""
            SELECT
              hci.user_id AS userId,
              COALESCE(NULLIF(u.nick_name, ''), '微信用户') AS nickName,
              u.avatar_url AS avatarUrl,
              COUNT(DISTINCT hci.check_in_date) AS rankValue
            FROM habit_check_in hci
            LEFT JOIN wx_user u ON u.id = hci.user_id
            GROUP BY hci.user_id, u.nick_name, u.avatar_url
            ORDER BY rankValue DESC, hci.user_id ASC
            LIMIT #{limit}
            """)
    List<UserCheckInRankItemResData> selectUserCheckInRankByDays(@Param("limit") Integer limit);

    @Select("""
            SELECT
              hci.user_id AS userId,
              COALESCE(NULLIF(u.nick_name, ''), '微信用户') AS nickName,
              u.avatar_url AS avatarUrl,
              COUNT(*) AS rankValue
            FROM habit_check_in hci
            LEFT JOIN wx_user u ON u.id = hci.user_id
            GROUP BY hci.user_id, u.nick_name, u.avatar_url
            ORDER BY rankValue DESC, hci.user_id ASC
            LIMIT #{limit}
            """)
    List<UserCheckInRankItemResData> selectUserCheckInRankByCount(@Param("limit") Integer limit);

    @Select("""
            SELECT
              user_id AS userId,
              COUNT(DISTINCT check_in_date) AS checkInDays,
              COUNT(*) AS checkInCount
            FROM habit_check_in
            GROUP BY user_id
            """)
    List<UserCheckInAggregateItem> selectUserCheckInAggregateItems();

    @Select("""
            SELECT
              hci.user_id AS userId,
              COALESCE(NULLIF(u.nick_name, ''), '微信用户') AS nickName,
              u.avatar_url AS avatarUrl,
              hci.check_in_date AS checkInDate
            FROM habit_check_in hci
            LEFT JOIN wx_user u ON u.id = hci.user_id
            GROUP BY hci.user_id, u.nick_name, u.avatar_url, hci.check_in_date
            ORDER BY hci.user_id ASC, hci.check_in_date ASC
            """)
    List<UserCheckInDateItem> selectUserCheckInDateItems();
}
