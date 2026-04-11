package com.nzhk.wxg.business.periodgoal.controller;

import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalDeleteReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalGetReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalItemResData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalListReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalListResData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalSaveReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalSaveResData;
import com.nzhk.wxg.business.periodgoal.service.IPeriodGoalService;
import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.exception.BizException;
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
@RequestMapping("/periodGoal")
public class PeriodGoalController {

    @Resource
    private IPeriodGoalService periodGoalService;

    @PostMapping("/list")
    public ResponseInfo<PeriodGoalListResData> list(@RequestBody RequestInfo<PeriodGoalListReqData> requestInfo) {
        try {
            String userId = ContextCache.getUserId();
            PeriodGoalListReqData data = requestInfo != null ? requestInfo.getData() : null;
            log.info("periodGoal list userId:{} type:{} {}~{}", userId,
                    data != null ? data.getPeriodType() : null,
                    data != null ? data.getPeriodStart() : null,
                    data != null ? data.getPeriodEnd() : null);
            return ResponseInfo.success(periodGoalService.list(userId, data));
        } catch (BizException e) {
            return ResponseInfo.fail(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.error("periodGoal list error", e);
            return ResponseInfo.fail(50000, "服务器异常", null);
        }
    }

    @PostMapping("/get")
    public ResponseInfo<PeriodGoalItemResData> get(@RequestBody RequestInfo<PeriodGoalGetReqData> requestInfo) {
        try {
            String userId = ContextCache.getUserId();
            PeriodGoalGetReqData data = requestInfo != null ? requestInfo.getData() : null;
            log.info("periodGoal get userId:{} id:{}", userId, data != null ? data.getId() : null);
            return ResponseInfo.success(periodGoalService.get(userId, data));
        } catch (BizException e) {
            return ResponseInfo.fail(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.error("periodGoal get error", e);
            return ResponseInfo.fail(50000, "服务器异常", null);
        }
    }

    @PostMapping("/save")
    public ResponseInfo<PeriodGoalSaveResData> save(@RequestBody RequestInfo<PeriodGoalSaveReqData> requestInfo) {
        try {
            String userId = ContextCache.getUserId();
            PeriodGoalSaveReqData data = requestInfo != null ? requestInfo.getData() : null;
            log.info("periodGoal save userId:{} id:{}", userId, data != null ? data.getId() : null);
            return ResponseInfo.success(periodGoalService.save(userId, data));
        } catch (BizException e) {
            return ResponseInfo.fail(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.error("periodGoal save error", e);
            return ResponseInfo.fail(50000, "服务器异常", null);
        }
    }

    @PostMapping("/delete")
    public ResponseInfo<Void> delete(@RequestBody RequestInfo<PeriodGoalDeleteReqData> requestInfo) {
        try {
            String userId = ContextCache.getUserId();
            PeriodGoalDeleteReqData data = requestInfo != null ? requestInfo.getData() : null;
            log.info("periodGoal delete userId:{} id:{}", userId, data != null ? data.getId() : null);
            periodGoalService.delete(userId, data);
            return ResponseInfo.success(null);
        } catch (BizException e) {
            return ResponseInfo.fail(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.error("periodGoal delete error", e);
            return ResponseInfo.fail(50000, "服务器异常", null);
        }
    }
}
