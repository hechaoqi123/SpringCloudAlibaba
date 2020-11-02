package com.example.consumer.controller.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author hcq
 * @date 2020/11/2 12:29
 */
@FeignClient(name = "provider" )
public interface ProviderService {

    @RequestMapping(value = "/provider/hello", method = RequestMethod.GET)
    String getBalance();
}
