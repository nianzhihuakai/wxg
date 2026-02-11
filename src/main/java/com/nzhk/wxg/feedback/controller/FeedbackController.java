package com.nzhk.wxg.feedback.controller;

import com.nzhk.wxg.common.info.RequestInfo;
import com.nzhk.wxg.common.info.ResponseInfo;
import com.nzhk.wxg.feedback.bean.SaveFeedbackReqData;
import com.nzhk.wxg.feedback.service.IFeedbackService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 建议与问题反馈表 前端控制器
 * </p>
 *
 * @author lxy
 * @since 2026-02-11
 */
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Resource
    private IFeedbackService feedbackService;

    @PostMapping("saveFeedback")
    public ResponseInfo<Void> saveFeedback (@RequestBody RequestInfo<SaveFeedbackReqData> requestInfo) {
        feedbackService.saveFeedback(requestInfo.getData());
        return ResponseInfo.success(null);
    }
}
