package com.campus.secondhand.user.service;

import java.util.Map;

public interface CaptchaService {

    Map<String, String> generate();

    boolean verify(String captchaKey, String captchaCode);
}
