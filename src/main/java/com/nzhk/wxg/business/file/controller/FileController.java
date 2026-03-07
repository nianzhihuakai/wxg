package com.nzhk.wxg.business.file.controller;

import com.nzhk.wxg.business.file.bean.FileUploadResData;
import com.nzhk.wxg.business.file.entity.UploadedFile;
import com.nzhk.wxg.business.file.service.IFileService;
import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.exception.BizException;
import com.nzhk.wxg.common.info.ResponseInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.nzhk.wxg.common.utils.FileSignUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private IFileService fileService;

    @Resource
    private FileSignUtil fileSignUtil;

    @PostMapping("/upload")
    public ResponseInfo<FileUploadResData> upload(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("bizType") String bizType,
                                                  @RequestParam(value = "clientTraceId", required = false) String clientTraceId) {
        try {
            String userId = ContextCache.getUserId();
            log.info("file upload request, userId:{}, bizType:{}, clientTraceId:{}", userId, bizType, clientTraceId);
            FileUploadResData data = fileService.upload(userId, file, bizType, clientTraceId);
            return ResponseInfo.success(data);
        } catch (BizException e) {
            return ResponseInfo.fail(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.error("file upload system error", e);
            return ResponseInfo.fail(50000, "服务器异常", null);
        }
    }

    @GetMapping("/access/{fileId}")
    public ResponseEntity<FileSystemResource> access(@PathVariable("fileId") String fileId,
                                                    @RequestParam(value = "sign", required = false) String sign,
                                                    @RequestParam(value = "expires", required = false) Long expires) {
        if (!fileSignUtil.validate(fileId, sign, expires)) {
            log.warn("file access unauthorized or expired, fileId={}", fileId);
            return ResponseEntity.status(403).build();
        }
        UploadedFile uploadedFile = fileService.getByFileId(fileId);
        if (uploadedFile == null) {
            return ResponseEntity.notFound().build();
        }
        Path filePath = Paths.get(uploadedFile.getStoragePath());
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (uploadedFile.getMimeType() != null) {
            try {
                mediaType = MediaType.parseMediaType(uploadedFile.getMimeType());
            } catch (Exception e) {
                log.warn("invalid mime type, fileId:{}, mimeType:{}", fileId, uploadedFile.getMimeType());
            }
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=2592000")
                .contentType(mediaType)
                .body(new FileSystemResource(filePath));
    }
}
