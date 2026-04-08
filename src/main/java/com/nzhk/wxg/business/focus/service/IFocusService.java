package com.nzhk.wxg.business.focus.service;

import com.nzhk.wxg.business.focus.bean.*;
import com.nzhk.wxg.business.focus.entity.FocusSession;

import java.util.List;

public interface IFocusService {
    FocusSessionResData start(FocusStartReqData data);
    FocusSessionResData pause(FocusActionReqData data);
    FocusSessionResData resume(FocusActionReqData data);
    FocusSessionResData adjust(FocusAdjustReqData data);
    FocusSessionResData finish(FocusFinishReqData data);
    FocusSessionResData current(FocusCurrentReqData data);
    FocusListResData list(FocusListReqData data);
    FocusStatsResData stats(FocusStatsReqData data);
    void delete(FocusActionReqData data);
    List<FocusSession> listNeedRemind();
    void markRemindSent(String sessionId);
}

