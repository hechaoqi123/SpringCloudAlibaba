package com.example.gateway.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * 限流、熔断、降级自定义处理策略
 * 例如：当接口限流时返回指定的json串，前端可以依据此响应报文做出相应处理（提示用户“当前系统繁忙，请稍后重试”）
 * TODO 异常处理
 * @author hcq
 * @date 2020/11/2 17:31
 */
@Component
public class CustomUrlBlockHandler implements BlockRequestHandler {

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        System.out.println(ex.getClass());
        return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                // 可以在此处构建统一的响应对象，也可以通过ex判断是限流还是降级
                .body(BodyInserters.fromObject(new Resp("ifule")));
    }

    public static class Resp {
        public String test;
        Resp(String t){
            test = t;
        }
    }

}
