package com.nzhk.wxg.business.habitcheckin.controller;

import com.nzhk.wxg.business.habitcheckin.bean.CheckInDetailReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInDetailResData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionGetReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionItemResData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionListReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionListResData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionSaveReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReqData;
import com.nzhk.wxg.business.habitcheckin.bean.StatisticsInfoResData;
import com.nzhk.wxg.business.habitcheckin.service.IHabitCheckInService;
import com.nzhk.wxg.common.info.RequestInfo;
import com.nzhk.wxg.common.info.ResponseInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("/habitCheckIn")
public class HabitCheckInController {

    @Resource
    private IHabitCheckInService habitCheckInService;

    @PostMapping("checkIn")
    public ResponseInfo<Void> checkIn (@RequestBody RequestInfo<CheckInReqData> requestInfo) {
        log.info("checkIn request, habitId:{}", requestInfo.getData() != null ? requestInfo.getData().getHabitId() : null);
        habitCheckInService.checkIn(requestInfo.getData());
        return ResponseInfo.success(null);
    }

    @PostMapping("fillCheckIn")
    public ResponseInfo<Void> fillCheckIn (@RequestBody RequestInfo<CheckInReqData> requestInfo) {
        log.info("fillCheckIn request, data:{}", requestInfo.getData() != null ? requestInfo.getData() : null);
        habitCheckInService.fillCheckIn(requestInfo.getData());
        return ResponseInfo.success(null);
    }

    @PostMapping("getWeekCheckInInfo")
    public ResponseInfo<CheckInDetailResData> getWeekCheckInInfo (@RequestBody RequestInfo<CheckInDetailReqData> requestInfo) {
        log.info("getWeekCheckInInfo request, habitId:{}, weekStart:{}, weekEnd:{}", requestInfo.getData() != null ? requestInfo.getData().getHabitId() : null, requestInfo.getData() != null ? requestInfo.getData().getWeekStart() : null, requestInfo.getData() != null ? requestInfo.getData().getWeekEnd() : null);
        return ResponseInfo.success(habitCheckInService.getWeekCheckInInfo(requestInfo.getData()));
    }

    @PostMapping("getMonthCheckInInfo")
    public ResponseInfo<CheckInDetailResData> getMonthCheckInInfo (@RequestBody RequestInfo<CheckInDetailReqData> requestInfo) {
        log.info("getMonthCheckInInfo request, habitId:{}, monthDate:{}", requestInfo.getData() != null ? requestInfo.getData().getHabitId() : null, requestInfo.getData() != null ? requestInfo.getData().getMonthDate() : null);
        return ResponseInfo.success(habitCheckInService.getMonthCheckInInfo(requestInfo.getData()));
    }

    @PostMapping("getYearCheckInInfo")
    public ResponseInfo<CheckInDetailResData> getYearCheckInInfo (@RequestBody RequestInfo<CheckInDetailReqData> requestInfo) {
        log.info("getYearCheckInInfo request, habitId:{}, yearDate:{}", requestInfo.getData() != null ? requestInfo.getData().getHabitId() : null, requestInfo.getData() != null ? requestInfo.getData().getYearDate() : null);
        return ResponseInfo.success(habitCheckInService.getYearCheckInInfo(requestInfo.getData()));
    }

    @PostMapping("getStatisticsInfo")
    public ResponseInfo<StatisticsInfoResData> getStatisticsInfo () {
        log.info("getStatisticsInfo request");
        return ResponseInfo.success(habitCheckInService.getStatisticsInfo());
    }

    @PostMapping("saveReflection")
    public ResponseInfo<Void> saveReflection(@RequestBody RequestInfo<CheckInReflectionSaveReqData> requestInfo) {
        log.info("saveReflection request");
        habitCheckInService.saveReflection(requestInfo.getData());
        return ResponseInfo.success(null);
    }

    @PostMapping("getReflection")
    public ResponseInfo<CheckInReflectionItemResData> getReflection(@RequestBody RequestInfo<CheckInReflectionGetReqData> requestInfo) {
        log.info("getReflection request");
        return ResponseInfo.success(habitCheckInService.getReflection(requestInfo.getData()));
    }

    @PostMapping("listReflections")
    public ResponseInfo<CheckInReflectionListResData> listReflections(@RequestBody RequestInfo<CheckInReflectionListReqData> requestInfo) {
        log.info("listReflections request");
        return ResponseInfo.success(habitCheckInService.listReflections(requestInfo.getData()));
    }
}
