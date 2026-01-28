package com.nzhk.wxg.business.habit.controller;

import com.nzhk.wxg.business.habit.bean.AddHabitReqData;
import com.nzhk.wxg.business.wxuser.bean.WxUserLoginReqData;
import com.nzhk.wxg.common.result.ResultInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("addHabit")
    public ResultInfo addHabit (@RequestBody AddHabitReqData addHabitReqData) {
        System.out.println(addHabitReqData);
        return ResultInfo.success(true);
    }
}
