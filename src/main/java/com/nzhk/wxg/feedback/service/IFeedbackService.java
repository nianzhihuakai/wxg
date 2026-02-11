package com.nzhk.wxg.feedback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nzhk.wxg.feedback.bean.SaveFeedbackReqData;
import com.nzhk.wxg.feedback.entity.Feedback;

/**
 * <p>
 * 建议与问题反馈表 服务类
 * </p>
 *
 * @author lxy
 * @since 2026-02-11
 */
public interface IFeedbackService extends IService<Feedback> {

    void saveFeedback(SaveFeedbackReqData data);
}
