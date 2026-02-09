package com.nzhk.wxg.business.habit.controller;

import com.nzhk.wxg.business.habit.bean.*;
import com.nzhk.wxg.business.habit.service.IHabitService;
import com.nzhk.wxg.common.info.RequestInfo;
import com.nzhk.wxg.common.info.ResponseInfo;
import jakarta.annotation.Resource;
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
@RestController
@RequestMapping("/habit")
public class HabitController {

    @Resource
    private IHabitService habitService;

    @PostMapping("getHabits")
    public ResponseInfo<List<HabitListResData>> getHabits (@RequestBody RequestInfo<HabitListReqData> requestInfo) {
        return ResponseInfo.success(habitService.getHabits(requestInfo.getData()));
    }

    @PostMapping("getHabitById")
    public ResponseInfo<HabitDetailResData> getHabitById (@RequestBody RequestInfo<HabitDetailReqData> requestInfo) {
        return ResponseInfo.success(habitService.getHabitById(requestInfo.getData()));
    }

    @PostMapping("addHabit")
    public ResponseInfo<Void> addHabit (@RequestBody RequestInfo<AddHabitReqData> requestInfo) {
        habitService.addHabit(requestInfo.getData());
        return ResponseInfo.success(null);
    }
}
