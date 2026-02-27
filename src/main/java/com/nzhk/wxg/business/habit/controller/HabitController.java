package com.nzhk.wxg.business.habit.controller;

import com.nzhk.wxg.business.habit.bean.*;
import com.nzhk.wxg.business.habit.service.IHabitService;
import com.nzhk.wxg.common.info.RequestInfo;
import com.nzhk.wxg.common.info.ResponseInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 习惯表 前端控制器
 * </p>
 *
 * @author lxy
 * @since 2026-01-28
 */
@Slf4j
@RestController
@RequestMapping("/habit")
public class HabitController {

    @Resource
    private IHabitService habitService;

    @PostMapping("getHabits")
    public ResponseInfo<List<HabitListResData>> getHabits (@RequestBody RequestInfo<HabitListReqData> requestInfo) {
        log.info("getHabits request, habitTypeId:{}", requestInfo.getData() != null ? requestInfo.getData().getHabitTypeId() : null);
        return ResponseInfo.success(habitService.getHabits(requestInfo.getData()));
    }

    @PostMapping("getArchiveHabits")
    public ResponseInfo<List<HabitListResData>> getArchiveHabits (@RequestBody RequestInfo<HabitListReqData> requestInfo) {
        log.info("getArchiveHabits request, requestInfo:{}", requestInfo.getData());
        return ResponseInfo.success(habitService.getArchiveHabits(requestInfo.getData()));
    }

    @PostMapping("getHabitById")
    public ResponseInfo<HabitDetailResData> getHabitById (@RequestBody RequestInfo<HabitDetailReqData> requestInfo) {
        log.info("getHabitById request, id:{}", requestInfo.getData() != null ? requestInfo.getData().getId() : null);
        return ResponseInfo.success(habitService.getHabitById(requestInfo.getData()));
    }

    @PostMapping("addHabit")
    public ResponseInfo<Void> addHabit (@RequestBody RequestInfo<AddHabitReqData> requestInfo) {
        log.info("addHabit request, data:{}", requestInfo.getData());
        habitService.addHabit(requestInfo.getData());
        return ResponseInfo.success(null);
    }

    @PostMapping("updateHabit")
    public ResponseInfo<Void> updateHabit (@RequestBody RequestInfo<UpdateHabitReqData> requestInfo) {
        log.info("updateHabit request:{}", requestInfo.getData());
        habitService.updateHabit(requestInfo.getData());
        return ResponseInfo.success(null);
    }

    @PostMapping("archiveHabit")
    public ResponseInfo<Void> archiveHabit (@RequestBody RequestInfo<UpdateHabitReqData> requestInfo) {
        log.info("archiveHabit request:{}", requestInfo.getData());
        habitService.archiveHabit(requestInfo.getData());
        return ResponseInfo.success(null);
    }

    @PostMapping("deleteHabit")
    public ResponseInfo<Void> deleteHabit (@RequestBody RequestInfo<UpdateHabitReqData> requestInfo) {
        log.info("deleteHabit request:{}", requestInfo.getData());
        habitService.deleteHabit(requestInfo.getData());
        return ResponseInfo.success(null);
    }
}
