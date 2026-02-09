package com.nzhk.wxg.business.habitcheckin.controller;

import com.nzhk.wxg.business.habitcheckin.bean.CheckInDetailReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInDetailResData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReqData;
import com.nzhk.wxg.business.habitcheckin.bean.StatisticsInfoResData;
import com.nzhk.wxg.business.habitcheckin.service.IHabitCheckInService;
import com.nzhk.wxg.common.info.RequestInfo;
import com.nzhk.wxg.common.info.ResponseInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 习惯打卡记录表 前端控制器
 * </p>
 *
 * @author lxy
 * @since 2026-02-06
 */
@RestController
@RequestMapping("/habitCheckIn")
public class HabitCheckInController {

    @Resource
    private IHabitCheckInService habitCheckInService;

    @PostMapping("checkIn")
    public ResponseInfo<Void> checkIn (@RequestBody RequestInfo<CheckInReqData> requestInfo) {
        habitCheckInService.checkIn(requestInfo.getData());
        return ResponseInfo.success(null);
    }

    @PostMapping("getWeekCheckInInfo")
    public ResponseInfo<CheckInDetailResData> getWeekCheckInInfo (@RequestBody RequestInfo<CheckInDetailReqData> requestInfo) {
        return ResponseInfo.success(habitCheckInService.getWeekCheckInInfo(requestInfo.getData()));
    }

    @PostMapping("getMonthCheckInInfo")
    public ResponseInfo<CheckInDetailResData> getMonthCheckInInfo (@RequestBody RequestInfo<CheckInDetailReqData> requestInfo) {
        return ResponseInfo.success(habitCheckInService.getMonthCheckInInfo(requestInfo.getData()));
    }

    @PostMapping("getYearCheckInInfo")
    public ResponseInfo<CheckInDetailResData> getYearCheckInInfo (@RequestBody RequestInfo<CheckInDetailReqData> requestInfo) {
        return ResponseInfo.success(habitCheckInService.getYearCheckInInfo(requestInfo.getData()));
    }

    @PostMapping("getStatisticsInfo")
    public ResponseInfo<StatisticsInfoResData> getStatisticsInfo () {
        return ResponseInfo.success(habitCheckInService.getStatisticsInfo());
    }
}
