package com.nzhk.wxg.business.journal.controller;

import com.nzhk.wxg.business.journal.bean.JournalDeleteReqData;
import com.nzhk.wxg.business.journal.bean.JournalDetailResData;
import com.nzhk.wxg.business.journal.bean.JournalListResData;
import com.nzhk.wxg.business.journal.bean.JournalSaveReqData;
import com.nzhk.wxg.business.journal.bean.JournalSaveResData;
import com.nzhk.wxg.business.journal.service.IJournalService;
import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.exception.BizException;
import com.nzhk.wxg.common.info.RequestInfo;
import com.nzhk.wxg.common.info.ResponseInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/list")
    public ResponseInfo<JournalListResData> list(@RequestParam("month") String month,
                                                 @RequestParam("pageNo") Integer pageNo,
                                                 @RequestParam("pageSize") Integer pageSize) {
        try {
            String userId = ContextCache.getUserId();
            log.info("journal list request, userId:{}, month:{}, pageNo:{}, pageSize:{}", userId, month, pageNo, pageSize);
            JournalListResData resData = journalService.list(userId, month, pageNo, pageSize);
            return ResponseInfo.success(resData);
        } catch (BizException e) {
            return ResponseInfo.fail(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.error("journal list system error", e);
            return ResponseInfo.fail(50000, "服务器异常", null);
        }
    }

    @GetMapping("/search")
    public ResponseInfo<JournalListResData> search(@RequestParam(value = "subject", required = false) String subject,
                                                   @RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "moodValue", required = false) String moodValue,
                                                   @RequestParam(value = "dateStart", required = false) String dateStart,
                                                   @RequestParam(value = "dateEnd", required = false) String dateEnd,
                                                   @RequestParam("pageNo") Integer pageNo,
                                                   @RequestParam("pageSize") Integer pageSize) {
        try {
            String userId = ContextCache.getUserId();
            log.info("journal search request, userId:{}, subject:{}, keyword:{}, moodValue:{}, dateStart:{}, dateEnd:{}, pageNo:{}, pageSize:{}",
                    userId, subject, keyword, moodValue, dateStart, dateEnd, pageNo, pageSize);
            JournalListResData resData = journalService.search(userId, subject, keyword, moodValue, dateStart, dateEnd, pageNo, pageSize);
            return ResponseInfo.success(resData);
        } catch (BizException e) {
            return ResponseInfo.fail(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.error("journal search system error", e);
            return ResponseInfo.fail(50000, "服务器异常", null);
        }
    }

    @GetMapping("/exportPdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam("month") String month) {
        try {
            String userId = ContextCache.getUserId();
            log.info("journal exportPdf request, userId:{}, month:{}", userId, month);
            byte[] pdfBytes = journalService.exportPdf(userId, month);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", month + "_journal.pdf");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (BizException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("journal exportPdf system error", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/delete")
    public ResponseInfo<Void> delete(@RequestBody RequestInfo<JournalDeleteReqData> requestInfo) {
        try {
            String userId = ContextCache.getUserId();
            JournalDeleteReqData data = requestInfo != null ? requestInfo.getData() : null;
            log.info("journal delete request, userId:{}, journalId:{}", userId, data != null ? data.getJournalId() : null);
            journalService.delete(userId, data);
            return ResponseInfo.success(null);
        } catch (BizException e) {
            return ResponseInfo.fail(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.error("journal delete system error", e);
            return ResponseInfo.fail(50000, "服务器异常", null);
        }
    }
}
