package com.nzhk.wxg.business.journal.service;

import com.nzhk.wxg.business.journal.bean.JournalDeleteReqData;
import com.nzhk.wxg.business.journal.bean.JournalDetailResData;
import com.nzhk.wxg.business.journal.bean.JournalListResData;
import com.nzhk.wxg.business.journal.bean.JournalSaveReqData;
import com.nzhk.wxg.business.journal.bean.JournalSaveResData;

public interface IJournalService {

    JournalSaveResData save(String userId, JournalSaveReqData data);

    JournalDetailResData getByDate(String userId, String date);

    JournalListResData list(String userId, String month, Integer pageNo, Integer pageSize);

    JournalListResData search(String userId, String subject, String keyword, String moodValue, String dateStart, String dateEnd, Integer pageNo, Integer pageSize);

    void delete(String userId, JournalDeleteReqData data);
}
