package com.nzhk.wxg.business.file.service;

import com.nzhk.wxg.business.file.bean.FileUploadResData;
import com.nzhk.wxg.business.file.entity.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFileService {

    FileUploadResData upload(String userId, MultipartFile file, String bizType, String clientTraceId);

    UploadedFile getByFileId(String fileId);

    /**
     * 按 fileId 列表物理删除 uploaded_file 记录
     * @param fileIds 文件业务ID列表，可为空
     */
    void deleteByFileIds(List<String> fileIds);
}
