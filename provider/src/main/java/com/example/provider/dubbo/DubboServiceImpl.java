package com.example.provider.dubbo;

import com.example.api.DubboService;
import org.apache.dubbo.config.annotation.Service;

/**
 * 通过接口的方式暴露Dubbo服务，consumer可以通过dubbo进行远程RPC
 * dubbo服务不对外暴露Http接口，内部调用相对于feign来说更加规范、也更加安全（入口统一）
 * dubbo基于tcp并且支持多种协议，feign基于Http1.1.相对来说dubbo更加灵活
 * @author hcq
 * @date 2020/11/2 13:44
 */
@Service
public class DubboServiceImpl implements DubboService{

    @Override
    public String sayHello() {
        return "Hello Dubbo";
    }
}
