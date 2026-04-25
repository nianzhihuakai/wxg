package com.nzhk.wxg.business.periodgoal.service;

import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalCountsInRangeReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalCountsInRangeResData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalDeleteReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalGetReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalItemResData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalListReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalListResData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalSaveReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalSaveResData;

public interface IPeriodGoalService {

    PeriodGoalListResData list(String userId, PeriodGoalListReqData data);

    /** 时间窗内各自然周期目标条数（日历弹层一次拉取） */
    PeriodGoalCountsInRangeResData countsInRange(String userId, PeriodGoalCountsInRangeReqData data);

    PeriodGoalItemResData get(String userId, PeriodGoalGetReqData data);

    PeriodGoalSaveResData save(String userId, PeriodGoalSaveReqData data);

    void delete(String userId, PeriodGoalDeleteReqData data);

    /** 从该用户所有有效周期目标的 habitIds 中移除指定习惯（删除习惯时调用）；若移除后无关联习惯则删除整条目标 */
    void removeHabitReference(String userId, String habitId);
}
