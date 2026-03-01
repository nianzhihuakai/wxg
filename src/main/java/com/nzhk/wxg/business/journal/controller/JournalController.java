package com.nzhk.wxg.business.journal.controller;

import com.nzhk.wxg.business.journal.bean.JournalDetailResData;
import com.nzhk.wxg.business.journal.bean.JournalSaveReqData;
import com.nzhk.wxg.business.journal.bean.JournalSaveResData;
import com.nzhk.wxg.business.journal.service.IJournalService;
import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.exception.BizException;
import com.nzhk.wxg.common.info.RequestInfo;
import com.nzhk.wxg.common.info.ResponseInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/journal")
public class JournalController {

    @Resource
    private IJournalService journalService;

    @PostMapping("/save")
    public ResponseInfo<JournalSaveResData> save(@RequestBody RequestInfo<JournalSaveReqData> requestInfo) {
        try {
            String userId = ContextCache.getUserId();
            JournalSaveReqData data = requestInfo != null ? requestInfo.getData() : null;
            log.info("journal save request, userId:{}, date:{}", userId, data != null ? data.getDate() : null);
            JournalSaveResData resData = journalService.save(userId, data);
            return ResponseInfo.success(resData);
        } catch (BizException e) {
            return ResponseInfo.fail(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.error("journal save system error", e);
            return ResponseInfo.fail(50000, "服务器异常", null);
        }
    }

    @GetMapping("/getByDate")
    public ResponseInfo<JournalDetailResData> getByDate(@RequestParam("date") String date) {
        try {
            String userId = ContextCache.getUserId();
            log.info("journal getByDate request, userId:{}, date:{}", userId, date);
            JournalDetailResData resData = journalService.getByDate(userId, date);
            return ResponseInfo.success(resData);
        } catch (BizException e) {
            return ResponseInfo.fail(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.error("journal getByDate system error", e);
            return ResponseInfo.fail(50000, "服务器异常", null);
        }
    }
}
