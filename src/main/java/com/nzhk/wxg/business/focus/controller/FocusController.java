package com.nzhk.wxg.business.focus.controller;

import com.nzhk.wxg.business.focus.bean.*;
import com.nzhk.wxg.business.focus.service.IFocusService;
import com.nzhk.wxg.common.info.RequestInfo;
import com.nzhk.wxg.common.info.ResponseInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/focus")
public class FocusController {
    @Resource
    private IFocusService focusService;

    @PostMapping("start")
    public ResponseInfo<FocusSessionResData> start(@RequestBody RequestInfo<FocusStartReqData> requestInfo) {
        return ResponseInfo.success(focusService.start(requestInfo.getData()));
    }

    @PostMapping("pause")
    public ResponseInfo<FocusSessionResData> pause(@RequestBody RequestInfo<FocusActionReqData> requestInfo) {
        return ResponseInfo.success(focusService.pause(requestInfo.getData()));
    }

    @PostMapping("resume")
    public ResponseInfo<FocusSessionResData> resume(@RequestBody RequestInfo<FocusActionReqData> requestInfo) {
        return ResponseInfo.success(focusService.resume(requestInfo.getData()));
    }

    @PostMapping("adjust")
    public ResponseInfo<FocusSessionResData> adjust(@RequestBody RequestInfo<FocusAdjustReqData> requestInfo) {
        return ResponseInfo.success(focusService.adjust(requestInfo.getData()));
    }

    @PostMapping("finish")
    public ResponseInfo<FocusSessionResData> finish(@RequestBody RequestInfo<FocusFinishReqData> requestInfo) {
        return ResponseInfo.success(focusService.finish(requestInfo.getData()));
    }

    @PostMapping("current")
    public ResponseInfo<FocusSessionResData> current(@RequestBody RequestInfo<FocusCurrentReqData> requestInfo) {
        return ResponseInfo.success(focusService.current(requestInfo.getData()));
    }

    @PostMapping("list")
    public ResponseInfo<FocusListResData> list(@RequestBody RequestInfo<FocusListReqData> requestInfo) {
        return ResponseInfo.success(focusService.list(requestInfo.getData()));
    }

    @PostMapping("stats")
    public ResponseInfo<FocusStatsResData> stats(@RequestBody RequestInfo<FocusStatsReqData> requestInfo) {
        return ResponseInfo.success(focusService.stats(requestInfo.getData()));
    }

    @PostMapping("delete")
    public ResponseInfo<Void> delete(@RequestBody RequestInfo<FocusActionReqData> requestInfo) {
        focusService.delete(requestInfo.getData());
        return ResponseInfo.success(null);
    }
}

