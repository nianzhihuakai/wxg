package com.nzhk.wxg.business.journal.service;

import com.nzhk.wxg.business.journal.bean.JournalDetailResData;
import com.nzhk.wxg.business.journal.bean.JournalSaveReqData;
import com.nzhk.wxg.business.journal.bean.JournalSaveResData;

public interface IJournalService {

    JournalSaveResData save(String userId, JournalSaveReqData data);

    JournalDetailResData getByDate(String userId, String date);
}
