package com.nzhk.wxg.business.habittype.controller;

import com.nzhk.wxg.business.habittype.bean.HabitTypeListReqData;
import com.nzhk.wxg.business.habittype.bean.UpdateHabitTypeReqData;
import com.nzhk.wxg.business.habittype.entity.HabitType;
import com.nzhk.wxg.business.habittype.service.IHabitTypeService;
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
 * 任务类型表：用户可自定义名称、图标与排序 前端控制器
 * </p>
 *
 * @author lxy
 * @since 2026-02-07
 */
@RestController
@RequestMapping("/habitType")
public class HabitTypeController {

    @Resource
    private IHabitTypeService habitTypeService;

    @PostMapping("getHabitTypes")
    public ResponseInfo<List<HabitType>> getHabitTypes (@RequestBody RequestInfo<HabitTypeListReqData> requestInfo) {
        return ResponseInfo.success(habitTypeService.getHabitTypes(requestInfo.getData()));
    }

    @PostMapping("addHabitType")
    public ResponseInfo<Void> addHabitType (@RequestBody RequestInfo<UpdateHabitTypeReqData> requestInfo) {
        habitTypeService.addHabitType(requestInfo.getData());
        return ResponseInfo.success(null);
    }

    @PostMapping("deleteHabitType")
    public ResponseInfo<Void> deleteHabitType (@RequestBody RequestInfo<UpdateHabitTypeReqData> requestInfo) {
        habitTypeService.deleteHabitType(requestInfo.getData());
        return ResponseInfo.success(null);
    }

    @PostMapping("updateHabitTypeOrder")
    public ResponseInfo<Void> updateHabitTypeOrder (@RequestBody RequestInfo<UpdateHabitTypeReqData> requestInfo) {
        habitTypeService.updateHabitTypeOrder(requestInfo.getData());
        return ResponseInfo.success(null);
    }

        @PostMapping("updateHabitType")
    public ResponseInfo<Void> updateHabitType (@RequestBody RequestInfo<UpdateHabitTypeReqData> requestInfo) {
        habitTypeService.updateHabitType(requestInfo.getData());
        return ResponseInfo.success(null);
    }
}
