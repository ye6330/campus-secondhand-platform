package com.campus.secondhand.user.service.impl;

import com.campus.secondhand.user.service.CaptchaService;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final long EXPIRE_MILLIS = 5 * 60 * 1000L;
    private static final ConcurrentHashMap<String, CaptchaValue> CAPTCHA_CACHE = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> generate() {
        clearExpired();
        String code = randomCode(4);
        String key = UUID.randomUUID().toString().replace("-", "");
        CAPTCHA_CACHE.put(key, new CaptchaValue(code, System.currentTimeMillis() + EXPIRE_MILLIS));
        return Map.of(
            "captchaKey", key,
            "imageBase64", "data:image/png;base64," + renderBase64(code)
        );
    }

    @Override
    public boolean verify(String captchaKey, String captchaCode) {
        clearExpired();
        if (captchaKey == null || captchaCode == null) {
            return false;
        }
        CaptchaValue value = CAPTCHA_CACHE.remove(captchaKey);
        if (value == null || value.expireAt < System.currentTimeMillis()) {
            return false;
        }
        return value.code.equalsIgnoreCase(captchaCode.trim());
    }

    private String randomCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * CHARACTERS.length());
            builder.append(CHARACTERS.charAt(index));
        }
        return builder.toString();
    }

    private String renderBase64(String code) {
        try {
            // 先创建一张固定大小的验证码画布
            BufferedImage image = new BufferedImage(120, 40, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();

            // 填充浅色背景，避免默认黑底影响显示
            graphics.setColor(new Color(245, 247, 250));
            graphics.fillRect(0, 0, 120, 40);

            // 设置验证码文字样式
            graphics.setFont(new Font("Arial", Font.BOLD, 24));

            // 逐个字符绘制，颜色和纵向位置略微错开，避免过于整齐
            for (int i = 0; i < code.length(); i++) {
                graphics.setColor(new Color(60 + i * 20, 90 + i * 10, 160 - i * 20));
                graphics.drawString(String.valueOf(code.charAt(i)), 18 + i * 22, 28 + (i % 2 == 0 ? 0 : 3));
            }

            // 叠加几条干扰线，降低脚本直接识别的成功率
            graphics.setColor(new Color(180, 190, 210));
            for (int i = 0; i < 6; i++) {
                int y1 = 5 + i * 5;
                int y2 = 8 + (i * 7) % 25;
                graphics.drawLine(0, y1, 120, y2);
            }

            // 释放画笔资源
            graphics.dispose();

            // 把内存中的图片编码成 PNG 字节，再转成 Base64 返回给前端 img 直接显示
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("生成验证码失败", e);
        }
    }

    private void clearExpired() {
        long now = System.currentTimeMillis();
        CAPTCHA_CACHE.entrySet().removeIf(entry -> entry.getValue().expireAt < now);
    }

    private static class CaptchaValue {
        private final String code;
        private final long expireAt;

        private CaptchaValue(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }
}
