package com.nzhk.wxg.business.periodgoal.service;

import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalDeleteReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalGetReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalItemResData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalListReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalListResData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalSaveReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalSaveResData;

public interface IPeriodGoalService {

    PeriodGoalListResData list(String userId, PeriodGoalListReqData data);

    PeriodGoalItemResData get(String userId, PeriodGoalGetReqData data);

    PeriodGoalSaveResData save(String userId, PeriodGoalSaveReqData data);

    void delete(String userId, PeriodGoalDeleteReqData data);
}
