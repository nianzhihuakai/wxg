package com.nzhk.wxg.wechat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * 微信消息推送接收接口
 * 用于小程序管理后台「开发-开发管理-消息推送配置」的 URL 验证及消息接收
 * <p>
 * 配置说明：
 * - URL: https://mini.nzhk.top/wxg/revice
 * - Token: wxg
 * - 消息加解密方式: 明文模式
 * - 数据格式: JSON
 *
 * @see <a href="https://developers.weixin.qq.com/miniprogram/dev/framework/server-ability/message-push.html">微信消息推送文档</a>
 */
@Slf4j
@RestController
@RequestMapping("/revice")
public class MessagePushController {

    private static final String TOKEN = "wxg";

    /**
     * 微信服务器验证接口（GET）
     * 配置消息推送时，微信会向该 URL 发起 GET 请求验证
     * 校验 signature 正确后，原样返回 echostr
     *
     * @param signature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param echostr   随机字符串，验证通过后需原样返回
     * @return 验证通过返回 echostr，否则返回空或错误信息
     */
    @GetMapping
    public String verify(@RequestParam("signature") String signature,
                         @RequestParam("timestamp") String timestamp,
                         @RequestParam("nonce") String nonce,
                         @RequestParam("echostr") String echostr) {
        log.info("WeChat message push verify: signature={}, timestamp={}, nonce={}, echostr={}",
                signature, timestamp, nonce, echostr);

        if (verifySignature(TOKEN, timestamp, nonce, signature)) {
            log.info("WeChat verify success, return echostr");
            return echostr;
        }

        log.warn("WeChat verify failed: signature mismatch");
        return "";
    }

    /**
     * 接收微信消息推送（POST）
     * 明文模式下，微信服务器将消息以 POST 发送到该 URL
     * 校验 signature 通过后，按业务处理；无特定要求时返回 success
     */
    @PostMapping
    public String receiveMessage(@RequestParam(value = "signature", required = false) String signature,
                                @RequestParam(value = "timestamp", required = false) String timestamp,
                                @RequestParam(value = "nonce", required = false) String nonce,
                                @RequestBody(required = false) String body) {
        log.info("WeChat message push received: signature={}, timestamp={}, nonce={}, body={}",
                signature, timestamp, nonce, body);

        if (signature != null && timestamp != null && nonce != null
                && verifySignature(TOKEN, timestamp, nonce, signature)) {
            // TODO: 根据 MsgType、Event 等解析 body 并处理业务
            return "success";
        }

        log.warn("WeChat message verify failed or params missing");
        return "success";
    }

    /**
     * 验证签名
     * 将 token、timestamp、nonce 三个参数进行字典序排序后拼接，进行 sha1 计算，与 signature 对比
     */
    private boolean verifySignature(String token, String timestamp, String nonce, String signature) {
        try {
            String[] arr = new String[]{token, timestamp, nonce};
            Arrays.sort(arr);
            String str = String.join("", arr);
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String computed = sb.toString();
            return computed.equals(signature);
        } catch (Exception e) {
            log.error("verify signature error", e);
            return false;
        }
    }
}
