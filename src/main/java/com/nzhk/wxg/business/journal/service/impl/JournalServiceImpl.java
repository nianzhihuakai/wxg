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
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.mapper.JournalImageMapper;
import com.nzhk.wxg.mapper.JournalMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JournalServiceImpl implements IJournalService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private JournalMapper journalMapper;

    @Resource
    private JournalImageMapper journalImageMapper;

    @Resource
    private IFileService fileService;

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
                imageResData.setUrl(item.getImageUrl());
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

    private void validateSaveRequest(JournalSaveReqData data) {
        if (data == null) {
            throw new BizException(40000, "请求体不能为空");
        }
        if (StringUtils.isBlank(data.getDate())) {
            throw new BizException(40000, "date 不能为空");
        }
        if (StringUtils.isBlank(data.getMoodValue())) {
            throw new BizException(40000, "moodValue 不能为空");
        }
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
            journalImage.setImageUrl(image.getUrl());
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
            imageResData.setUrl(image.getImageUrl());
            imageResData.setSort(image.getSortOrder());
            imageResData.setWidth(image.getWidth());
            imageResData.setHeight(image.getHeight());
            imageMap.computeIfAbsent(image.getJournalId(), k -> new ArrayList<>()).add(imageResData);
        }
        return imageMap;
    }
}
