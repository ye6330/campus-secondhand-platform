package com.campus.secondhand.user.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.campus.secondhand.user.config.OssProperties;
import com.campus.secondhand.user.service.FileService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OssFileServiceImpl implements FileService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final OssProperties ossProperties;

    public OssFileServiceImpl(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new RuntimeException("仅支持 jpg、png、webp 格式的图片");
        }
        validateOssConfig();

        String extension = getExtension(contentType);
        String dir = ossProperties.getDir() == null ? "user/" : ossProperties.getDir();
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }
        String objectName = dir + "avatars/"
            + LocalDate.now().format(DATE_FORMATTER) + "/"
            + UUID.randomUUID().toString().replace("-", "") + extension;

        OSS ossClient = new OSSClientBuilder().build(
            ossProperties.getEndpoint(),
            ossProperties.getAccessKeyId(),
            ossProperties.getAccessKeySecret()
        );
        try {
            ossClient.putObject(ossProperties.getBucket(), objectName, file.getInputStream());
            return "https://" + ossProperties.getBucket() + "." + ossProperties.getEndpoint() + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("头像上传失败", e);
        } finally {
            ossClient.shutdown();
        }
    }

    private void validateOssConfig() {
        if (isBlank(ossProperties.getAccessKeyId()) || isBlank(ossProperties.getAccessKeySecret())) {
            throw new IllegalStateException("请先配置 OSS 的 AccessKeyId 和 AccessKeySecret");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty() || value.startsWith("YOUR_");
    }

    private String getExtension(String contentType) {
        switch (contentType) {
            case "image/jpeg": return ".jpg";
            case "image/png":  return ".png";
            case "image/webp": return ".webp";
            default:           return ".jpg";
        }
    }
}
