package com.example.consumer.controller.feign;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author hcq
 * @date 2020/11/2 12:25
 */
@RestController
@RequestMapping("/feign")
public class ConsumerController {

    @Resource
    ProviderService providerService;

    @RequestMapping("/consumer")
    public String consumer(){
        return providerService.getBalance();
    }
}
