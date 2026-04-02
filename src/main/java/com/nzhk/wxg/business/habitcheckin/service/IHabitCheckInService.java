package com.nzhk.wxg.business.habitcheckin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInDetailReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInDetailResData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionGetReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionItemResData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionListReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionListResData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionSaveReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReqData;
import com.nzhk.wxg.business.habitcheckin.bean.StatisticsInfoResData;
import com.nzhk.wxg.business.habitcheckin.entity.HabitCheckIn;

/**
 * <p>
 * 习惯打卡记录表 服务类
 * </p>
 *
 * @author lxy
 * @since 2026-02-06
 */
public interface IHabitCheckInService extends IService<HabitCheckIn> {

    void checkIn(CheckInReqData data);

    void fillCheckIn(CheckInReqData data);

    CheckInDetailResData getWeekCheckInInfo(CheckInDetailReqData data);

    CheckInDetailResData getMonthCheckInInfo(CheckInDetailReqData data);

    CheckInDetailResData getYearCheckInInfo(CheckInDetailReqData data);

    StatisticsInfoResData getStatisticsInfo();

    void saveReflection(CheckInReflectionSaveReqData data);

    CheckInReflectionItemResData getReflection(CheckInReflectionGetReqData data);

    CheckInReflectionListResData listReflections(CheckInReflectionListReqData data);
}
