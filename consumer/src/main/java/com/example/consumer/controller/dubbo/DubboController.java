package com.example.consumer.controller.dubbo;

import com.example.api.DubboService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hcq
 * @date 2020/11/2 14:39
 */
@RestController
@RequestMapping("dubbo")
public class DubboController {

    @Reference
    private  DubboService dubboService;

    @RequestMapping("/hello")
    public String hello(){
        return dubboService.sayHello();
    }
}
