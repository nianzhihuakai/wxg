package com.nzhk.wxg.feedback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.feedback.bean.SaveFeedbackReqData;
import com.nzhk.wxg.feedback.entity.Feedback;
import com.nzhk.wxg.feedback.service.IFeedbackService;
import com.nzhk.wxg.mapper.FeedbackMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 建议与问题反馈表 服务实现类
 * </p>
 *
 * @author lxy
 * @since 2026-02-11
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements IFeedbackService {

    @Override
    public void saveFeedback(SaveFeedbackReqData data) {
        Feedback feedback = new Feedback();
        feedback.setId(IdUtil.getId());
        feedback.setUserId(ContextCache.getUserId());
        feedback.setContent(data.getContent());
        baseMapper.insert(feedback);
    }
}
