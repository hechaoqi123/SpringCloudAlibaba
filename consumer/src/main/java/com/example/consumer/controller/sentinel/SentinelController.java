package com.example.consumer.controller.sentinel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * sentinel效果演示相关接口
 * @author hcq
 * @date 2020/11/3 10:16
 */
@RestController
@RequestMapping("/sentinel")
public class SentinelController {

    /**
     * 演示sentinel接口降级规则
     * [
     *   {
     *     "resource": "/sentinel/degrade",
     *     "count": 200,
     *     "grade": 0,
     *     "timeWindow": 120,
     *     "minRequestAmount": 1,
     *     "desc":"当/sentinel/degrade接口响应时长大于200ms的请求数大于minRequestAmount时，对当前接口进行降级120s"
     *   }
     * ]
     * @throws InterruptedException 线程中断异常
     */
    @GetMapping("/degrade")
    public String degrade() throws InterruptedException {
        Thread.sleep(300);
        return "hello world";
    }
}
