package com.huaweicloud.samples.mesher.consumer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "python-app")
public interface PythonAppClient {

    @GetMapping("/show")
    Map<String, Object> show();
}
