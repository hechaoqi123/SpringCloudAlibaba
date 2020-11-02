package com.example.consumer.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 限流、熔断、降级自定义处理策略
 * 例如：当接口限流时返回指定的json串，前端可以依据此响应报文做出相应处理（提示用户“当前系统繁忙，请稍后重试”）
 * @author hcq
 * @date 2020/11/2 17:31
 */
@Component
public class CustomUrlBlockHandler implements UrlBlockHandler {
    @Override
    public void blocked(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws IOException {
        httpServletResponse.setHeader("Content-Type","application/json;charset=UTF-8");
        String message = "{\"code\":999,\"msg\":\"访问人数过多\"}";
        //使用类创建就json对象
        httpServletResponse.getWriter().write(message);
    }
}
