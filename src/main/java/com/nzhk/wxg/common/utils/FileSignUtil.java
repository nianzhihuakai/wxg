package com.nzhk.wxg.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 文件访问签名 URL 工具，用于生成和校验带签名的临时访问链接
 */
@Slf4j
@Component
public class FileSignUtil {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${wxg.file.base-url:/wxg/file/access/}")
    private String baseUrl;

    @Value("${wxg.file.sign-secret:wxg-file-default-secret-change-in-prod}")
    private String signSecret;

    @Value("${wxg.file.sign-expire-seconds:3600}")
    private int signExpireSeconds;

    /**
     * 生成带签名的访问 URL，格式：baseUrl + fileId + ?sign=xxx&expires=yyy
     */
    public String generateSignedUrl(String fileId) {
        if (StringUtils.isBlank(fileId)) {
            return null;
        }
        long expires = System.currentTimeMillis() / 1000 + signExpireSeconds;
        String sign = computeSign(fileId, expires);
        String base = StringUtils.appendIfMissing(baseUrl, "/");
        return base + fileId + "?sign=" + sign + "&expires=" + expires;
    }

    /**
     * 校验 sign 和 expires 是否有效
     */
    public boolean validate(String fileId, String sign, Long expires) {
        if (StringUtils.isBlank(fileId) || StringUtils.isBlank(sign) || expires == null) {
            return false;
        }
        if (expires < System.currentTimeMillis() / 1000) {
            log.warn("file access expired, fileId={}", fileId);
            return false;
        }
        String expected = computeSign(fileId, expires);
        return expected != null && expected.equals(sign);
    }

    /**
     * 将可能带签名的 URL 规范化为存储用的基础路径（去除 sign、expires 等），用于持久化
     */
    public String toStoredUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }
        String fileId = extractFileIdFromUrl(url);
        if (fileId == null) {
            return url;
        }
        String base = StringUtils.appendIfMissing(baseUrl, "/");
        return base + fileId;
    }

    /**
     * 若 url 为本站文件访问路径，则替换为带签名的 URL；否则原样返回
     */
    public String signFileUrlIfNeeded(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }
        String fileId = extractFileIdFromUrl(url);
        if (fileId == null) {
            return url;
        }
        return generateSignedUrl(fileId);
    }

    /**
     * 从 URL 中提取 fileId，如 /wxg/file/access/f_xxx 或 https://xxx/wxg/file/access/f_xxx?x=1
     */
    private String extractFileIdFromUrl(String url) {
        int idx = url.indexOf("/file/access/");
        if (idx < 0) {
            return null;
        }
        String rest = url.substring(idx + "/file/access/".length()).trim();
        int q = rest.indexOf('?');
        if (q >= 0) {
            rest = rest.substring(0, q);
        }
        rest = rest.replaceAll("/", "").trim();
        return StringUtils.isNotBlank(rest) ? rest : null;
    }

    private String computeSign(String fileId, long expires) {
        try {
            String data = fileId + "|" + expires;
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(signSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("compute sign failed", e);
            return null;
        }
    }
}
