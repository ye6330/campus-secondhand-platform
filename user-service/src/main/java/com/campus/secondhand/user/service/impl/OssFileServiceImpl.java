package com.campus.secondhand.user.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.campus.secondhand.user.config.OssProperties;
import com.campus.secondhand.user.service.FileService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OssFileServiceImpl implements FileService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final OssProperties ossProperties;
    private OSS ossClient;

    public OssFileServiceImpl(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @PostConstruct
    public void init() {
        this.ossClient = new OSSClientBuilder().build(
            ossProperties.getEndpoint(),
            ossProperties.getAccessKeyId(),
            ossProperties.getAccessKeySecret()
        );
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new RuntimeException("仅支持 jpg、png、webp 格式的图片");
        }

        String extension = getExtension(contentType);
        String objectName = ossProperties.getDir() + "avatars/"
            + LocalDate.now().format(DATE_FORMATTER) + "/"
            + UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            ossClient.putObject(ossProperties.getBucket(), objectName, file.getInputStream());
            return "https://" + ossProperties.getBucket() + "." + ossProperties.getEndpoint() + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("头像上传失败", e);
        }
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
