package com.nzhk.wxg.business.file.service;

import com.nzhk.wxg.business.file.bean.FileUploadResData;
import com.nzhk.wxg.business.file.entity.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {

    FileUploadResData upload(String userId, MultipartFile file, String bizType, String clientTraceId);

    UploadedFile getByFileId(String fileId);
}
