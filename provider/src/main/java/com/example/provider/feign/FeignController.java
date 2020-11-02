package com.example.provider.feign;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 暴露HTTP接口，consumer可以通过feign组件调用此项目提供的HTTP服务
 * @author hcq
 * @date 2020/11/2 12:14
 */
@RequestMapping("/provider")
@RestController
public class FeignController {

    @RequestMapping("hello")
    public String consumer(){
        return "hello Feign";
    }

}
