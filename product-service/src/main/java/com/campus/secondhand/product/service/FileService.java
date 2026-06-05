package com.campus.secondhand.product.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String uploadProductImage(MultipartFile file);
}
