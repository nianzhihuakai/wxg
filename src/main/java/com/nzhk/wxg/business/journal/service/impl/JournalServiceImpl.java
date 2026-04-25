package com.nzhk.wxg.business.journal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nzhk.wxg.business.file.entity.UploadedFile;
import com.nzhk.wxg.business.file.service.IFileService;
import com.nzhk.wxg.business.journal.bean.JournalDeleteReqData;
import com.nzhk.wxg.business.journal.bean.JournalDetailResData;
import com.nzhk.wxg.business.journal.bean.JournalImageReqData;
import com.nzhk.wxg.business.journal.bean.JournalImageResData;
import com.nzhk.wxg.business.journal.bean.JournalHistoryItemResData;
import com.nzhk.wxg.business.journal.bean.JournalListResData;
import com.nzhk.wxg.business.journal.bean.JournalSaveReqData;
import com.nzhk.wxg.business.journal.bean.JournalSaveResData;
import com.nzhk.wxg.business.journal.entity.Journal;
import com.nzhk.wxg.business.journal.entity.JournalImage;
import com.nzhk.wxg.business.journal.service.IJournalService;
import com.nzhk.wxg.common.exception.BizException;
import com.nzhk.wxg.common.utils.FileSignUtil;
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.mapper.JournalImageMapper;
import com.nzhk.wxg.mapper.JournalMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JournalServiceImpl implements IJournalService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 心情编码与文案（与小程序端一致）；mood_value 多个时用英文逗号拼接 */
    private static final Map<String, String> JOURNAL_MOOD_CODE_TO_LABEL;

    private static final int JOURNAL_MOOD_MAX_COUNT = 3;

    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("happy", "开心");
        m.put("calm", "平静");
        m.put("excited", "期待");
        m.put("tired", "疲惫");
        m.put("anxious", "焦虑");
        m.put("grateful", "感恩");
        m.put("fulfilled", "充实");
        m.put("motivated", "有干劲");
        m.put("relaxed", "放松");
        m.put("sad", "难过");
        m.put("angry", "生气");
        m.put("bored", "无聊");
        JOURNAL_MOOD_CODE_TO_LABEL = Collections.unmodifiableMap(m);
    }

    @Resource
    private JournalMapper journalMapper;

    @Resource
    private JournalImageMapper journalImageMapper;

    @Resource
    private IFileService fileService;

    @Resource
    private FileSignUtil fileSignUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JournalSaveResData save(String userId, JournalSaveReqData data) {
        validateSaveRequest(data);
        LocalDate journalDate = parseDate(data.getDate());

        if (StringUtils.isNotBlank(data.getClientRequestId())) {
            Journal existedByRequestId = findByClientRequestId(userId, data.getClientRequestId());
            if (existedByRequestId != null) {
                return buildSaveRes(existedByRequestId);
            }
        }

        Journal journal = findTargetJournal(userId, journalDate, data.getJournalId());
        LocalDateTime now = LocalDateTime.now();
        if (journal == null) {
            journal = new Journal();
            journal.setId(IdUtil.getId());
            journal.setUserId(userId);
            journal.setJournalDate(journalDate);
            journal.setStatus(1);
            journal.setCreatedAt(now);
            fillJournalFields(journal, data, now);
            journalMapper.insert(journal);
        } else {
            fillJournalFields(journal, data, now);
            LambdaUpdateWrapper<Journal> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Journal::getId, journal.getId())
                    .eq(Journal::getUserId, userId)
                    .eq(Journal::getStatus, 1);
            journalMapper.update(journal, updateWrapper);
        }

        replaceJournalImages(userId, journal.getId(), data.getImages());
        return buildSaveRes(journal);
    }

    @Override
    public JournalDetailResData getByDate(String userId, String date) {
        LocalDate journalDate = parseDate(date);
        LambdaQueryWrapper<Journal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Journal::getUserId, userId)
                .eq(Journal::getJournalDate, journalDate)
                .eq(Journal::getStatus, 1)
                .last("limit 1");
        Journal journal = journalMapper.selectOne(wrapper);
        if (journal == null) {
            throw new BizException(40400, "日记不存在");
        }

        JournalDetailResData resData = new JournalDetailResData();
        resData.setJournalId(journal.getId());
        resData.setDate(journal.getJournalDate().format(DATE_FORMATTER));
        resData.setMoodValue(journal.getMoodValue());
        resData.setMoodLabel(journal.getMoodLabel());
        resData.setSubject(journal.getSubject());
        resData.setContent(journal.getContent());

        LambdaQueryWrapper<JournalImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(JournalImage::getJournalId, journal.getId())
                .eq(JournalImage::getUserId, userId)
                .eq(JournalImage::getStatus, 1)
                .orderByAsc(JournalImage::getSortOrder)
                .orderByAsc(JournalImage::getId);
        List<JournalImage> journalImages = journalImageMapper.selectList(imageWrapper);
        List<JournalImageResData> images = new ArrayList<>();
        if (!CollectionUtils.isEmpty(journalImages)) {
            for (JournalImage item : journalImages) {
                JournalImageResData imageResData = new JournalImageResData();
                imageResData.setFileId(item.getFileId());
                imageResData.setUrl(fileSignUtil.generateSignedUrl(item.getFileId()));
                imageResData.setSort(item.getSortOrder());
                imageResData.setWidth(item.getWidth());
                imageResData.setHeight(item.getHeight());
                images.add(imageResData);
            }
        }
        resData.setImages(images);
        return resData;
    }

    @Override
    public JournalListResData list(String userId, String month, Integer pageNo, Integer pageSize) {
        LocalDate monthStart = parseMonthStart(month);
        validatePage(pageNo, pageSize);
        LocalDate nextMonthStart = monthStart.plusMonths(1);
        int offset = (pageNo - 1) * pageSize;

        LambdaQueryWrapper<Journal> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Journal::getUserId, userId)
                .eq(Journal::getStatus, 1)
                .ge(Journal::getJournalDate, monthStart)
                .lt(Journal::getJournalDate, nextMonthStart);
        Long total = journalMapper.selectCount(countWrapper);
        if (total == null) {
            total = 0L;
        }

        List<JournalHistoryItemResData> records = new ArrayList<>();
        if (total > 0) {
            LambdaQueryWrapper<Journal> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Journal::getUserId, userId)
                    .eq(Journal::getStatus, 1)
                    .ge(Journal::getJournalDate, monthStart)
                    .lt(Journal::getJournalDate, nextMonthStart)
                    .orderByDesc(Journal::getJournalDate)
                    .orderByDesc(Journal::getUpdatedAt)
                    .last("limit " + pageSize + " offset " + offset);
            List<Journal> journals = journalMapper.selectList(wrapper);
            if (!CollectionUtils.isEmpty(journals)) {
                Map<String, List<JournalImageResData>> imageMap = queryImageMap(userId, journals);
                for (Journal journal : journals) {
                    JournalHistoryItemResData item = new JournalHistoryItemResData();
                    item.setJournalId(journal.getId());
                    item.setDate(journal.getJournalDate().format(DATE_FORMATTER));
                    item.setMoodValue(journal.getMoodValue());
                    item.setMoodLabel(journal.getMoodLabel());
                    item.setSubject(journal.getSubject());
                    item.setContent(journal.getContent());
                    item.setUpdatedAt(journal.getUpdatedAt() == null ? null : journal.getUpdatedAt().toString());
                    item.setImages(imageMap.getOrDefault(journal.getId(), Collections.emptyList()));
                    records.add(item);
                }
            }
        }

        JournalListResData resData = new JournalListResData();
        resData.setPageNo(pageNo);
        resData.setPageSize(pageSize);
        resData.setTotal(total);
        resData.setHasMore((long) pageNo * pageSize < total);
        resData.setRecords(records);
        return resData;
    }

    @Override
    public JournalListResData search(String userId, String subject, String keyword, String moodValue, String dateStart, String dateEnd, Integer pageNo, Integer pageSize) {
        boolean hasSubject = StringUtils.isNotBlank(subject);
        boolean hasKeyword = StringUtils.isNotBlank(keyword);
        boolean hasMood = StringUtils.isNotBlank(moodValue);
        boolean hasDateStart = StringUtils.isNotBlank(dateStart);
        boolean hasDateEnd = StringUtils.isNotBlank(dateEnd);
        if (!hasSubject && !hasKeyword && !hasMood && !hasDateStart && !hasDateEnd) {
            throw new BizException(40000, "请至少选择一个搜索条件：年月、心情、主题或内容");
        }
        validatePage(pageNo, pageSize);
        int offset = (pageNo - 1) * pageSize;

        LambdaQueryWrapper<Journal> countWrapper = buildSearchWrapper(userId, subject, keyword, moodValue, dateStart, dateEnd);
        Long total = journalMapper.selectCount(countWrapper);
        if (total == null) {
            total = 0L;
        }

        List<JournalHistoryItemResData> records = new ArrayList<>();
        if (total > 0) {
            LambdaQueryWrapper<Journal> wrapper = buildSearchWrapper(userId, subject, keyword, moodValue, dateStart, dateEnd);
            wrapper.orderByDesc(Journal::getJournalDate)
                    .orderByDesc(Journal::getUpdatedAt)
                    .last("limit " + pageSize + " offset " + offset);
            List<Journal> journals = journalMapper.selectList(wrapper);
            if (!CollectionUtils.isEmpty(journals)) {
                Map<String, List<JournalImageResData>> imageMap = queryImageMap(userId, journals);
                for (Journal journal : journals) {
                    JournalHistoryItemResData item = new JournalHistoryItemResData();
                    item.setJournalId(journal.getId());
                    item.setDate(journal.getJournalDate().format(DATE_FORMATTER));
                    item.setMoodValue(journal.getMoodValue());
                    item.setMoodLabel(journal.getMoodLabel());
                    item.setSubject(journal.getSubject());
                    item.setContent(journal.getContent());
                    item.setUpdatedAt(journal.getUpdatedAt() == null ? null : journal.getUpdatedAt().toString());
                    item.setImages(imageMap.getOrDefault(journal.getId(), Collections.emptyList()));
                    records.add(item);
                }
            }
        }

        JournalListResData resData = new JournalListResData();
        resData.setPageNo(pageNo);
        resData.setPageSize(pageSize);
        resData.setTotal(total);
        resData.setHasMore((long) pageNo * pageSize < total);
        resData.setRecords(records);
        return resData;
    }

    private LambdaQueryWrapper<Journal> buildSearchWrapper(String userId, String subject, String keyword, String moodValue, String dateStart, String dateEnd) {
        LambdaQueryWrapper<Journal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Journal::getUserId, userId).eq(Journal::getStatus, 1);
        if (StringUtils.isNotBlank(subject)) {
            wrapper.like(Journal::getSubject, subject.trim());
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Journal::getContent, keyword.trim());
        }
        if (StringUtils.isNotBlank(moodValue)) {
            String m = moodValue.trim();
            // mood_value 可为逗号分隔多心情：匹配包含其中某一种编码的记录（PostgreSQL）
            wrapper.and(w -> w.eq(Journal::getMoodValue, m)
                    .or().likeRight(Journal::getMoodValue, m + ",")
                    .or().likeLeft(Journal::getMoodValue, "," + m)
                    .or().like(Journal::getMoodValue, "," + m + ","));
        }
        if (StringUtils.isNotBlank(dateStart)) {
            LocalDate start = parseDate(dateStart);
            wrapper.ge(Journal::getJournalDate, start);
        }
        if (StringUtils.isNotBlank(dateEnd)) {
            LocalDate end = parseDate(dateEnd);
            wrapper.le(Journal::getJournalDate, end);
        }
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String userId, JournalDeleteReqData data) {
        if (data == null || StringUtils.isBlank(data.getJournalId())) {
            throw new BizException(40000, "journalId 不能为空");
        }
        String journalId = data.getJournalId();
        Journal journal = findTargetJournal(userId, null, journalId);
        if (journal == null) {
            throw new BizException(40400, "日记不存在");
        }
        LambdaQueryWrapper<JournalImage> imageQueryWrapper = new LambdaQueryWrapper<>();
        imageQueryWrapper.eq(JournalImage::getJournalId, journalId).eq(JournalImage::getUserId, userId);
        List<JournalImage> toDeleteImages = journalImageMapper.selectList(imageQueryWrapper);
        List<String> fileIds = toDeleteImages.stream()
                .map(JournalImage::getFileId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        journalImageMapper.delete(imageQueryWrapper);
        journalMapper.deleteById(journalId);
        fileService.deleteByFileIds(fileIds);
    }

    @Override
    public byte[] exportPdf(String userId, String month) {
        LocalDate monthStart = parseMonthStart(month);
        LocalDate nextMonthStart = monthStart.plusMonths(1);
        LambdaQueryWrapper<Journal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Journal::getUserId, userId)
                .eq(Journal::getStatus, 1)
                .ge(Journal::getJournalDate, monthStart)
                .lt(Journal::getJournalDate, nextMonthStart)
                .orderByAsc(Journal::getJournalDate);
        List<Journal> journals = journalMapper.selectList(wrapper);
        return buildPdf(userId, month, journals);
    }

    private byte[] buildPdf(String userId, String month, List<Journal> journals) {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 54, 54, 54, 54);
            com.lowagie.text.pdf.PdfWriter writer = com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);
            com.lowagie.text.pdf.BaseFont bf;
            try {
                bf = com.lowagie.text.pdf.BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", com.lowagie.text.pdf.BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                log.warn("Chinese font not available, using Helvetica", e);
                bf = com.lowagie.text.pdf.BaseFont.createFont(com.lowagie.text.pdf.BaseFont.HELVETICA, com.lowagie.text.pdf.BaseFont.WINANSI, com.lowagie.text.pdf.BaseFont.NOT_EMBEDDED);
            }
            String monthTitle = formatMonthTitle(month);
            String footerMonthLine = monthTitle + " · WXG 日记";
            writer.setPageEvent(new JournalExportPdfPageEvent(bf, footerMonthLine));
            document.open();
            java.awt.Color titleColor = new java.awt.Color(45, 55, 72);
            java.awt.Color metaColor = new java.awt.Color(107, 114, 128);
            java.awt.Color contentColor = new java.awt.Color(75, 85, 99);
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(bf, 22, com.lowagie.text.Font.BOLD, titleColor);
            com.lowagie.text.Font metaFont = new com.lowagie.text.Font(bf, 11, com.lowagie.text.Font.NORMAL, metaColor);
            com.lowagie.text.Font subjectFont = new com.lowagie.text.Font(bf, 14, com.lowagie.text.Font.BOLD, titleColor);
            com.lowagie.text.Font contentFont = new com.lowagie.text.Font(bf, 12, com.lowagie.text.Font.NORMAL, contentColor);
            com.lowagie.text.Font tocTitleFont = new com.lowagie.text.Font(bf, 16, com.lowagie.text.Font.BOLD, titleColor);
            com.lowagie.text.Font tocItemFont = new com.lowagie.text.Font(bf, 10, com.lowagie.text.Font.NORMAL, metaColor);
            com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph(monthTitle + " 日记", titleFont);
            title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(8);
            document.add(title);
            String exportDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日"));
            com.lowagie.text.Paragraph subTitle = new com.lowagie.text.Paragraph("导出日期：" + exportDate, metaFont);
            subTitle.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(10);
            document.add(subTitle);
            if (!CollectionUtils.isEmpty(journals)) {
                com.lowagie.text.Paragraph countLine = new com.lowagie.text.Paragraph("共 " + journals.size() + " 篇", metaFont);
                countLine.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                countLine.setSpacingAfter(16);
                document.add(countLine);
            }
            com.lowagie.text.pdf.draw.LineSeparator sep = new com.lowagie.text.pdf.draw.LineSeparator();
            sep.setLineColor(new java.awt.Color(229, 231, 235));
            sep.setPercentage(100);
            sep.setLineWidth(0.5f);
            document.add(sep);
            document.add(new com.lowagie.text.Paragraph(" "));
            if (CollectionUtils.isEmpty(journals)) {
                com.lowagie.text.Paragraph emptyTip = new com.lowagie.text.Paragraph("本月暂无日记", contentFont);
                emptyTip.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                emptyTip.setSpacingBefore(40);
                document.add(emptyTip);
            } else {
                addJournalTableOfContents(document, journals, tocTitleFont, tocItemFont);
                Map<String, List<JournalImage>> imageMap = queryImagesForExport(userId, journals);
                for (Journal j : journals) {
                    String dateStr = j.getJournalDate().format(DATE_FORMATTER);
                    String subject = StringUtils.trimToNull(j.getSubject());
                    String mood = StringUtils.defaultString(j.getMoodLabel(), j.getMoodValue());
                    addJournalHeaderBlock(document, dateStr, mood, subject, metaFont, subjectFont);
                    List<String> paragraphs = splitHtmlToParagraphTexts(StringUtils.defaultString(j.getContent(), ""));
                    for (int i = 0; i < paragraphs.size(); i++) {
                        String block = paragraphs.get(i);
                        if (StringUtils.isBlank(block)) {
                            continue;
                        }
                        com.lowagie.text.Paragraph contentPara = new com.lowagie.text.Paragraph(block, contentFont);
                        contentPara.setAlignment(com.lowagie.text.Element.ALIGN_JUSTIFIED);
                        contentPara.setLeading(20);
                        boolean last = i == paragraphs.size() - 1;
                        contentPara.setSpacingAfter(last ? 12 : 6);
                        document.add(contentPara);
                    }
                    List<JournalImage> images = imageMap.getOrDefault(j.getId(), Collections.emptyList());
                    addImagesToPdf(document, images);
                    document.add(new com.lowagie.text.Paragraph(" "));
                }
            }
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("journal exportPdf error", e);
            throw new BizException(50000, "导出PDF失败");
        }
    }

    /**
     * 将 yyyy-MM 转为「yyyy年M月」用于封面标题（与 footer 一致风格）。
     */
    private String formatMonthTitle(String month) {
        if (StringUtils.isBlank(month)) {
            return month;
        }
        try {
            LocalDate first = LocalDate.parse(month + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return first.getYear() + "年" + first.getMonthValue() + "月";
        } catch (Exception e) {
            return month;
        }
    }

    private void addJournalTableOfContents(com.lowagie.text.Document document, List<Journal> journals,
            com.lowagie.text.Font tocTitleFont, com.lowagie.text.Font tocItemFont) throws com.lowagie.text.DocumentException {
        com.lowagie.text.Paragraph tocTitle = new com.lowagie.text.Paragraph("目录", tocTitleFont);
        tocTitle.setSpacingAfter(10);
        document.add(tocTitle);
        for (Journal j : journals) {
            String subj = StringUtils.isNotBlank(j.getSubject()) ? j.getSubject() : "（无标题）";
            String line = j.getJournalDate().format(DATE_FORMATTER) + "    " + subj;
            com.lowagie.text.Paragraph linePara = new com.lowagie.text.Paragraph(line, tocItemFont);
            linePara.setLeading(15);
            document.add(linePara);
        }
        document.add(new com.lowagie.text.Paragraph(" "));
        com.lowagie.text.pdf.draw.LineSeparator tocSep = new com.lowagie.text.pdf.draw.LineSeparator();
        tocSep.setLineColor(new java.awt.Color(229, 231, 235));
        tocSep.setPercentage(100);
        tocSep.setLineWidth(0.5f);
        document.add(tocSep);
        document.add(new com.lowagie.text.Paragraph(" "));
    }

    /**
     * 日期单独一行、心情单独一行；表头使用 {@link com.lowagie.text.pdf.PdfPTable#setKeepTogether(boolean)}，减少日期与标题被分页拆开。
     */
    private void addJournalHeaderBlock(com.lowagie.text.Document document, String dateStr, String mood, String subject,
            com.lowagie.text.Font metaFont, com.lowagie.text.Font subjectFont) throws com.lowagie.text.DocumentException {
        com.lowagie.text.pdf.PdfPTable headerTable = new com.lowagie.text.pdf.PdfPTable(1);
        headerTable.setWidthPercentage(100f);
        headerTable.setSpacingBefore(18f);
        headerTable.setKeepTogether(true);
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell();
        cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        cell.setPadding(0);
        com.lowagie.text.Paragraph dateLine = new com.lowagie.text.Paragraph(dateStr, metaFont);
        dateLine.setSpacingAfter(2);
        cell.addElement(dateLine);
        if (StringUtils.isNotBlank(mood)) {
            com.lowagie.text.Paragraph moodLine = new com.lowagie.text.Paragraph(mood, metaFont);
            moodLine.setSpacingAfter(6);
            cell.addElement(moodLine);
        }
        if (StringUtils.isNotBlank(subject)) {
            com.lowagie.text.Paragraph subjectLine = new com.lowagie.text.Paragraph(subject, subjectFont);
            subjectLine.setSpacingAfter(8);
            cell.addElement(subjectLine);
        } else {
            com.lowagie.text.Paragraph subjectLine = new com.lowagie.text.Paragraph("（无标题）", metaFont);
            subjectLine.setSpacingAfter(8);
            cell.addElement(subjectLine);
        }
        headerTable.addCell(cell);
        document.add(headerTable);
    }

    /**
     * 按 HTML 块级/换行拆成多段纯文本，避免整篇挤成一段。
     */
    private List<String> splitHtmlToParagraphTexts(String html) {
        if (StringUtils.isBlank(html)) {
            return Collections.emptyList();
        }
        String h = html;
        h = h.replaceAll("(?i)<br\\s*/?>", "\n");
        h = h.replaceAll("(?i)</div\\s*>", "\n");
        h = h.replaceAll("(?i)<div[^>]*>", "");
        h = h.replaceAll("(?i)</p\\s*>", "\n\n");
        h = h.replaceAll("(?i)<p[^>]*>", "");
        h = h.replaceAll("(?i)</li\\s*>", "\n");
        h = h.replaceAll("(?i)<li[^>]*>", "· ");
        h = h.replaceAll("<[^>]+>", " ");
        h = decodeBasicHtmlEntities(h);
        h = h.replaceAll("[ \t\f\r\u00a0]+", " ");
        h = h.replaceAll(" *\n *", "\n");
        h = h.replaceAll("\n{3,}", "\n\n");
        String[] blocks = h.split("\n\n+");
        List<String> out = new ArrayList<>();
        for (String block : blocks) {
            if (block == null) {
                continue;
            }
            String normalized = block.replace('\n', ' ').replaceAll(" +", " ").trim();
            if (StringUtils.isNotBlank(normalized)) {
                out.add(normalized);
            }
        }
        return out;
    }

    private String decodeBasicHtmlEntities(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&nbsp;", " ")
                .replace("&#160;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }

    /**
     * 校验 moodValue：1~3 个已知编码，英文逗号分隔；并规范化 moodValue、moodLabel 写入 data。
     */
    private void validateAndNormalizeJournalMoods(JournalSaveReqData data) {
        String raw = data.getMoodValue();
        if (StringUtils.isBlank(raw)) {
            throw new BizException(40000, "moodValue 不能为空");
        }
        String[] split = raw.split(",");
        List<String> codes = new ArrayList<>();
        for (String part : split) {
            String t = StringUtils.trimToNull(part);
            if (t == null) {
                continue;
            }
            if (!JOURNAL_MOOD_CODE_TO_LABEL.containsKey(t)) {
                throw new BizException(40000, "moodValue 含有不支持的编码: " + t);
            }
            if (!codes.contains(t)) {
                codes.add(t);
            }
        }
        if (codes.isEmpty()) {
            throw new BizException(40000, "moodValue 不能为空");
        }
        if (codes.size() > JOURNAL_MOOD_MAX_COUNT) {
            throw new BizException(40000, "心情最多选择 " + JOURNAL_MOOD_MAX_COUNT + " 个");
        }
        data.setMoodValue(String.join(",", codes));
        data.setMoodLabel(codes.stream().map(JOURNAL_MOOD_CODE_TO_LABEL::get).collect(Collectors.joining("、")));
    }

    private void validateSaveRequest(JournalSaveReqData data) {
        if (data == null) {
            throw new BizException(40000, "请求体不能为空");
        }
        if (StringUtils.isBlank(data.getDate())) {
            throw new BizException(40000, "date 不能为空");
        }
        validateAndNormalizeJournalMoods(data);
        if (StringUtils.isBlank(data.getSubject())) {
            throw new BizException(40000, "subject 不能为空");
        }
        if (StringUtils.isBlank(data.getContent())) {
            throw new BizException(40000, "content 不能为空");
        }
        if (data.getSubject().length() > 64) {
            throw new BizException(40000, "subject 长度不能超过 64");
        }
        if (data.getContent().length() > 5000) {
            throw new BizException(40000, "content 长度不能超过 5000");
        }
        if (!CollectionUtils.isEmpty(data.getImages()) && data.getImages().size() > 9) {
            throw new BizException(40000, "图片数量不能超过 9 张");
        }
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BizException(40000, "date 格式错误，需为 yyyy-MM-dd");
        }
    }

    private LocalDate parseMonthStart(String month) {
        if (StringUtils.isBlank(month)) {
            throw new BizException(40000, "month 不能为空");
        }
        try {
            return LocalDate.parse(month + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new BizException(40000, "month 格式错误，应为 yyyy-MM");
        }
    }

    private void validatePage(Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo <= 0) {
            throw new BizException(40000, "pageNo 必须从 1 开始");
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            throw new BizException(40000, "pageSize 范围应为 1~100");
        }
    }

    private Journal findByClientRequestId(String userId, String clientRequestId) {
        LambdaQueryWrapper<Journal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Journal::getUserId, userId)
                .eq(Journal::getClientRequestId, clientRequestId)
                .eq(Journal::getStatus, 1)
                .orderByDesc(Journal::getUpdatedAt)
                .last("limit 1");
        return journalMapper.selectOne(wrapper);
    }

    private Journal findTargetJournal(String userId, LocalDate journalDate, String journalId) {
        if (StringUtils.isNotBlank(journalId)) {
            LambdaQueryWrapper<Journal> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Journal::getId, journalId)
                    .eq(Journal::getUserId, userId)
                    .eq(Journal::getStatus, 1)
                    .last("limit 1");
            Journal journal = journalMapper.selectOne(wrapper);
            if (journal == null) {
                throw new BizException(40400, "日记不存在");
            }
            return journal;
        }

        LambdaQueryWrapper<Journal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Journal::getUserId, userId)
                .eq(Journal::getJournalDate, journalDate)
                .eq(Journal::getStatus, 1)
                .last("limit 1");
        return journalMapper.selectOne(wrapper);
    }

    private void fillJournalFields(Journal journal, JournalSaveReqData data, LocalDateTime now) {
        journal.setMoodValue(data.getMoodValue());
        journal.setMoodLabel(data.getMoodLabel());
        journal.setSubject(data.getSubject());
        journal.setContent(data.getContent());
        journal.setClientRequestId(data.getClientRequestId());
        journal.setUpdatedAt(now);
    }

    private void replaceJournalImages(String userId, String journalId, List<JournalImageReqData> images) {
        LambdaQueryWrapper<JournalImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JournalImage::getJournalId, journalId).eq(JournalImage::getUserId, userId);
        List<JournalImage> toDeleteImages = journalImageMapper.selectList(queryWrapper);
        List<String> oldFileIds = toDeleteImages.stream()
                .map(JournalImage::getFileId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Set<String> keepFileIds = CollectionUtils.isEmpty(images) ? Collections.emptySet()
                : images.stream()
                        .filter(img -> img != null && StringUtils.isNotBlank(img.getFileId()))
                        .map(JournalImageReqData::getFileId)
                        .collect(Collectors.toSet());
        List<String> fileIdsToDelete = oldFileIds.stream()
                .filter(id -> !keepFileIds.contains(id))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(images)) {
            images.sort(Comparator.comparing(JournalImageReqData::getSort, Comparator.nullsLast(Integer::compareTo)));
            // 先校验所有图片，确保文件存在且归属正确，再执行删除，避免误删后校验失败
            for (JournalImageReqData image : images) {
                if (image == null || StringUtils.isBlank(image.getFileId()) || StringUtils.isBlank(image.getUrl()) || image.getSort() == null) {
                    throw new BizException(40000, "images 参数不完整");
                }
                if (image.getSort() <= 0) {
                    throw new BizException(40000, "images.sort 必须从 1 开始");
                }
                UploadedFile uploadedFile = fileService.getByFileId(image.getFileId());
                if (uploadedFile == null || !StringUtils.equals(uploadedFile.getUserId(), userId)) {
                    throw new BizException(40000, "图片文件不存在或不属于当前用户");
                }
            }
        }

        journalImageMapper.delete(queryWrapper);
        fileService.deleteByFileIds(fileIdsToDelete);

        if (CollectionUtils.isEmpty(images)) {
            return;
        }

        for (int i = 0; i < images.size(); i++) {
            JournalImageReqData image = images.get(i);
            UploadedFile uploadedFile = fileService.getByFileId(image.getFileId());

            JournalImage journalImage = new JournalImage();
            journalImage.setId(IdUtil.getId());
            journalImage.setJournalId(journalId);
            journalImage.setUserId(userId);
            journalImage.setFileId(image.getFileId());
            journalImage.setImageUrl(fileSignUtil.toStoredUrl(image.getUrl()));
            journalImage.setSortOrder(image.getSort());
            journalImage.setWidth(image.getWidth());
            journalImage.setHeight(image.getHeight());
            journalImage.setFileSize(image.getFileSize() != null ? image.getFileSize() : uploadedFile.getFileSize());
            journalImage.setMimeType(StringUtils.isNotBlank(image.getMimeType()) ? image.getMimeType() : uploadedFile.getMimeType());
            journalImage.setStatus(1);
            journalImage.setCreatedAt(LocalDateTime.now());
            journalImageMapper.insert(journalImage);
        }
    }

    private JournalSaveResData buildSaveRes(Journal journal) {
        JournalSaveResData resData = new JournalSaveResData();
        resData.setJournalId(journal.getId());
        resData.setDate(journal.getJournalDate().format(DATE_FORMATTER));
        LocalDateTime updatedAt = journal.getUpdatedAt();
        if (updatedAt != null) {
            resData.setUpdatedAt(updatedAt.toString());
        }
        return resData;
    }

    private Map<String, List<JournalImage>> queryImagesForExport(String userId, List<Journal> journals) {
        if (CollectionUtils.isEmpty(journals)) {
            return Collections.emptyMap();
        }
        List<String> journalIds = journals.stream().map(Journal::getId).collect(Collectors.toList());
        LambdaQueryWrapper<JournalImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(JournalImage::getUserId, userId)
                .eq(JournalImage::getStatus, 1)
                .in(JournalImage::getJournalId, journalIds)
                .orderByAsc(JournalImage::getSortOrder)
                .orderByAsc(JournalImage::getId);
        List<JournalImage> journalImages = journalImageMapper.selectList(imageWrapper);
        Map<String, List<JournalImage>> imageMap = new HashMap<>();
        for (JournalImage image : journalImages) {
            imageMap.computeIfAbsent(image.getJournalId(), k -> new ArrayList<>()).add(image);
        }
        return imageMap;
    }

    private void addImagesToPdf(com.lowagie.text.Document document, List<JournalImage> images) {
        if (CollectionUtils.isEmpty(images)) {
            return;
        }
        float contentWidth = document.right() - document.left();
        if (contentWidth < 120f) {
            contentWidth = 400f;
        }
        int imageCount = 0;
        for (JournalImage img : images) {
            if (img != null && StringUtils.isNotBlank(img.getFileId())) {
                imageCount++;
            }
        }
        if (imageCount == 0) {
            return;
        }
        int cols = imageCount <= 2 ? 1 : 3;
        float gutter = 8f;
        float cellOuterW = (contentWidth - gutter * (cols - 1)) / cols;
        float maxCellW = Math.max(60f, cellOuterW - 10f);
        float maxCellH = cols == 1 ? Math.min(380f, maxCellW * 1.15f) : Math.min(200f, maxCellW * 0.95f);
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(cols);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(8);
        table.setSpacingAfter(8);
        table.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        table.setSplitLate(true);
        table.setSplitRows(false);
        int cellsAdded = 0;
        for (JournalImage img : images) {
            if (img == null || StringUtils.isBlank(img.getFileId())) {
                continue;
            }
            try {
                UploadedFile uploadedFile = fileService.getByFileId(img.getFileId());
                if (uploadedFile == null || StringUtils.isBlank(uploadedFile.getStoragePath())) {
                    log.warn("pdf export: file not found, fileId={}", img.getFileId());
                    continue;
                }
                Path path = Paths.get(uploadedFile.getStoragePath());
                if (!Files.exists(path) || !Files.isReadable(path)) {
                    log.warn("pdf export: file not readable, path={}", uploadedFile.getStoragePath());
                    continue;
                }
                byte[] bytes = Files.readAllBytes(path);
                com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(bytes);
                if (image.getWidth() > maxCellW || image.getHeight() > maxCellH) {
                    image.scaleToFit(maxCellW, maxCellH);
                }
                image.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(image, true);
                cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                cell.setPadding(4);
                table.addCell(cell);
                cellsAdded++;
            } catch (Exception e) {
                log.warn("pdf export: add image failed, fileId={}", img.getFileId(), e);
            }
        }
        if (cellsAdded == 0) {
            return;
        }
        while (cellsAdded % cols != 0) {
            com.lowagie.text.pdf.PdfPCell emptyCell = new com.lowagie.text.pdf.PdfPCell();
            emptyCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            emptyCell.setMinimumHeight(1);
            table.addCell(emptyCell);
            cellsAdded++;
        }
        document.add(table);
    }

    private Map<String, List<JournalImageResData>> queryImageMap(String userId, List<Journal> journals) {
        List<String> journalIds = journals.stream().map(Journal::getId).collect(Collectors.toList());
        LambdaQueryWrapper<JournalImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(JournalImage::getUserId, userId)
                .eq(JournalImage::getStatus, 1)
                .in(JournalImage::getJournalId, journalIds)
                .orderByAsc(JournalImage::getSortOrder)
                .orderByAsc(JournalImage::getId);
        List<JournalImage> journalImages = journalImageMapper.selectList(imageWrapper);
        Map<String, List<JournalImageResData>> imageMap = new HashMap<>();
        if (CollectionUtils.isEmpty(journalImages)) {
            return imageMap;
        }
        for (JournalImage image : journalImages) {
            JournalImageResData imageResData = new JournalImageResData();
            imageResData.setFileId(image.getFileId());
            imageResData.setUrl(fileSignUtil.generateSignedUrl(image.getFileId()));
            imageResData.setSort(image.getSortOrder());
            imageResData.setWidth(image.getWidth());
            imageResData.setHeight(image.getHeight());
            imageMap.computeIfAbsent(image.getJournalId(), k -> new ArrayList<>()).add(imageResData);
        }
        return imageMap;
    }

    /**
     * 每页底部显示「第 k / n 页」（n 为整份 PDF 总页数）与月份副标题；封面与正文可从同一页起排。
     */
    private static final class JournalExportPdfPageEvent extends com.lowagie.text.pdf.PdfPageEventHelper {

        private static final float FOOTER_MAIN_SIZE = 9f;
        private static final float FOOTER_SUB_SIZE = 8f;

        private final com.lowagie.text.pdf.BaseFont bf;
        private final String monthFooterLine;
        private com.lowagie.text.pdf.PdfTemplate totalPagesTemplate;

        JournalExportPdfPageEvent(com.lowagie.text.pdf.BaseFont bf, String monthFooterLine) {
            this.bf = bf;
            this.monthFooterLine = monthFooterLine;
        }

        @Override
        public void onOpenDocument(com.lowagie.text.pdf.PdfWriter writer, com.lowagie.text.Document document) {
            totalPagesTemplate = writer.getDirectContent().createTemplate(48, 20);
        }

        @Override
        public void onEndPage(com.lowagie.text.pdf.PdfWriter writer, com.lowagie.text.Document document) {
            int currentPage = writer.getPageNumber();
            String prefix = "第 " + currentPage + " 页 / ";
            com.lowagie.text.pdf.PdfContentByte cb = writer.getDirectContent();
            float centerX = (document.left() + document.right()) / 2f;
            float yMain = document.bottom() - 22f;
            float ySub = document.bottom() - 36f;
            float prefixW = bf.getWidthPoint(prefix, FOOTER_MAIN_SIZE);
            float startX = centerX - prefixW / 2f - 10f;
            cb.saveState();
            cb.setColorFill(new java.awt.Color(107, 114, 128));
            cb.beginText();
            cb.setFontAndSize(bf, FOOTER_MAIN_SIZE);
            cb.setTextMatrix(startX, yMain);
            cb.showText(prefix);
            cb.endText();
            cb.addTemplate(totalPagesTemplate, startX + prefixW, yMain);
            if (StringUtils.isNotBlank(monthFooterLine)) {
                cb.beginText();
                cb.setFontAndSize(bf, FOOTER_SUB_SIZE);
                cb.showTextAligned(com.lowagie.text.Element.ALIGN_CENTER, monthFooterLine, centerX, ySub, 0);
                cb.endText();
            }
            cb.restoreState();
        }

        @Override
        public void onCloseDocument(com.lowagie.text.pdf.PdfWriter writer, com.lowagie.text.Document document) {
            int total = Math.max(1, writer.getPageNumber());
            totalPagesTemplate.beginText();
            totalPagesTemplate.setFontAndSize(bf, FOOTER_MAIN_SIZE);
            totalPagesTemplate.setTextMatrix(0, 3);
            totalPagesTemplate.showText(String.valueOf(total));
            totalPagesTemplate.endText();
        }
    }
}
