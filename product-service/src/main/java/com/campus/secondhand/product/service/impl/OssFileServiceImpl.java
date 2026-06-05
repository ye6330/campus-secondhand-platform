package com.campus.secondhand.product.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.campus.secondhand.product.config.OssProperties;
import com.campus.secondhand.product.service.FileService;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OssFileServiceImpl implements FileService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final OssProperties ossProperties;

    public OssFileServiceImpl(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public String uploadProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请先选择要上传的图片");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("仅支持 jpg、png、webp 图片上传");
        }
        validateOssConfig();

        String objectKey = buildObjectKey(file.getOriginalFilename());
        OSS ossClient = new OSSClientBuilder().build(
            ossProperties.getEndpoint(),
            ossProperties.getAccessKeyId(),
            ossProperties.getAccessKeySecret()
        );

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            ossClient.putObject(ossProperties.getBucket(), objectKey, inputStream, metadata);
            return "https://" + ossProperties.getBucket() + "." + ossProperties.getEndpoint() + "/" + objectKey;
        } catch (OSSException | IOException e) {
            throw new RuntimeException("图片上传到 OSS 失败", e);
        } finally {
            ossClient.shutdown();
        }
    }

    private void validateOssConfig() {
        if (isBlank(ossProperties.getAccessKeyId()) || isBlank(ossProperties.getAccessKeySecret())) {
            throw new IllegalStateException("请先在 application.yml 中填写 OSS 的 AccessKeyId 和 AccessKeySecret");
        }
    }

    private String buildObjectKey(String originalFilename) {
        String suffix = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String dir = ossProperties.getDir() == null ? "products/" : ossProperties.getDir();
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }
        return dir + UUID.randomUUID().toString().replace("-", "") + suffix;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty() || value.startsWith("YOUR_");
    }
}
