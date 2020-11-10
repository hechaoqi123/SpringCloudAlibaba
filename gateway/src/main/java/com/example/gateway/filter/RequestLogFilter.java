package com.example.gateway.filter;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 打印请求信息及响应信息log
 *
 * @author pangu
 * @since 2020-7-16
 */
@Component
@Slf4j
public class RequestLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 生成唯一id用于帮忙定位日志信息
        String requestId = UUID.randomUUID().toString().replace("-", "");
        // 获取Request信息
        ServerHttpRequest request = new ServerHttpRequestDecorator(exchange.getRequest());
        printRequestInfo(requestId,request);
        // 获取Response信息
        ServerHttpResponseDecorator resp = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.map(dataBuffer -> {
                        // probably should reuse buffers
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        //释放掉内存
                        DataBufferUtils.release(dataBuffer);
                        log.info("【Response】 ===>  【ResponseBody】 {} 【RequestId】 {}",  new String(content,Charsets.UTF_8), requestId);
                        byte[] uppedContent = new String(content, Charsets.UTF_8).getBytes();
                        return bufferFactory().wrap(uppedContent);
                    }));
                }
                return super.writeWith(body);
            }
        };
        return chain.filter(exchange.mutate()
                .request(request)
                .response(resp)
                .build());
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

    /**
     * 打印入参
     * @param requestId 唯一标识
     * @param request 请求信息
     */
    private void printRequestInfo(String requestId,ServerHttpRequest request) {
        URI requestUri = request.getURI();
        String uriQuery = requestUri.getQuery();
        String url = requestUri.getPath() + (StringUtils.isNotBlank(uriQuery) ? "?" + uriQuery : "");
        HttpHeaders headers = request.getHeaders();
        MediaType mediaType = headers.getContentType();
        String method = request.getMethodValue().toUpperCase();
        String reqBody = null;
        if (Objects.nonNull(mediaType) && isUploadFile(mediaType)) {
            reqBody = "文件上传。。";
        } else {
            if (HttpMethod.GET.name().equals(method)) {
                if (StringUtils.isNotBlank(uriQuery)) {
                    reqBody = uriQuery;
                }
            } else if (headers.getContentLength() > 0) {
                AtomicReference<String> bodyString = new AtomicReference<>();
                request.getBody().subscribe(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    bodyString.set(new String(bytes,  Charsets.UTF_8));
                });
                reqBody = bodyString.get();
            }
        }
        log.info("【Request】 ===> 【Method】 {} 【Path】 {} 【RequestBody】 {} 【RequestId】 {}", method, url, reqBody, requestId);
    }


    /**
     * 判断是否是上传文件
     *
     * @param mediaType MediaType
     * @return Boolean
     */
    private static boolean isUploadFile(@Nullable MediaType mediaType) {
        if (Objects.isNull(mediaType)) {
            return false;
        }
        String mediaTypeStr = mediaType.toString();
        // 处理类似multipart/form-data; boundary=<calculated when request is sent>的情况
        mediaTypeStr = mediaTypeStr.split(";")[0];
        return mediaTypeStr.equals(MediaType.MULTIPART_FORM_DATA.toString())
                || mediaTypeStr.equals(MediaType.IMAGE_GIF.toString())
                || mediaTypeStr.equals(MediaType.IMAGE_JPEG.toString())
                || mediaTypeStr.equals(MediaType.IMAGE_PNG.toString())
                || mediaTypeStr.equals(MediaType.MULTIPART_MIXED.toString());
        // || FILE_CONTENT_TYPE.containsValue(mediaTypeStr)
    }
}
