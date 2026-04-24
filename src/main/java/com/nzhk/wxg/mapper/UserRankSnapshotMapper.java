package com.nzhk.wxg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nzhk.wxg.business.habitcheckin.bean.UserCheckInRankItemResData;
import com.nzhk.wxg.business.habitcheckin.entity.UserRankSnapshot;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserRankSnapshotMapper extends BaseMapper<UserRankSnapshot> {

    @Delete("DELETE FROM user_rank_snapshot")
    void deleteAllSnapshots();

    @Insert("""
            INSERT INTO user_rank_snapshot (
              user_id, check_in_days, check_in_count, current_streak_days, max_streak_days, momentum_score, last_check_in_date, updated_at
            ) VALUES (
              #{userId}, #{checkInDays}, #{checkInCount}, #{currentStreakDays}, #{maxStreakDays}, #{momentumScore}, #{lastCheckInDate}, NOW()
            )
            ON CONFLICT (user_id) DO UPDATE SET
              check_in_days = EXCLUDED.check_in_days,
              check_in_count = EXCLUDED.check_in_count,
              current_streak_days = EXCLUDED.current_streak_days,
              max_streak_days = EXCLUDED.max_streak_days,
              momentum_score = EXCLUDED.momentum_score,
              last_check_in_date = EXCLUDED.last_check_in_date,
              updated_at = NOW()
            """)
    void upsertSnapshot(UserRankSnapshot snapshot);

    @Select("""
            SELECT
              s.user_id AS userId,
              COALESCE(NULLIF(u.nick_name, ''), '微信用户') AS nickName,
              u.avatar_url AS avatarUrl,
              s.check_in_days AS rankValue,
              s.current_streak_days AS currentStreakDays,
              s.max_streak_days AS maxStreakDays
            FROM user_rank_snapshot s
            LEFT JOIN wx_user u ON u.id = s.user_id
            ORDER BY s.check_in_days DESC, s.user_id ASC
            LIMIT #{limit}
            """)
    List<UserCheckInRankItemResData> selectRankByDays(@Param("limit") Integer limit);

    @Select("""
            SELECT
              s.user_id AS userId,
              COALESCE(NULLIF(u.nick_name, ''), '微信用户') AS nickName,
              u.avatar_url AS avatarUrl,
              s.check_in_count AS rankValue,
              s.current_streak_days AS currentStreakDays,
              s.max_streak_days AS maxStreakDays
            FROM user_rank_snapshot s
            LEFT JOIN wx_user u ON u.id = s.user_id
            ORDER BY s.check_in_count DESC, s.user_id ASC
            LIMIT #{limit}
            """)
    List<UserCheckInRankItemResData> selectRankByCount(@Param("limit") Integer limit);

    @Select("""
            SELECT
              s.user_id AS userId,
              COALESCE(NULLIF(u.nick_name, ''), '微信用户') AS nickName,
              u.avatar_url AS avatarUrl,
              ROUND(s.momentum_score) AS rankValue,
              s.current_streak_days AS currentStreakDays,
              s.max_streak_days AS maxStreakDays
            FROM user_rank_snapshot s
            LEFT JOIN wx_user u ON u.id = s.user_id
            ORDER BY s.momentum_score DESC, s.current_streak_days DESC, s.max_streak_days DESC, s.user_id ASC
            LIMIT #{limit}
            """)
    List<UserCheckInRankItemResData> selectRankByMomentum(@Param("limit") Integer limit);
}
