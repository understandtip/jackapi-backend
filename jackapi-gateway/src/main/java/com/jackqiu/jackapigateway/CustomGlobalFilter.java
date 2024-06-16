package com.jackqiu.jackapigateway;

import com.jackqiu.jackapi.jackapibackendsdk.utils.SignUtil;
import com.jackqiu.jackapi.model.entity.InterfaceInfo;
import com.jackqiu.jackapi.model.entity.User;
import com.jackqiu.jackapi.model.entity.UserInterfaceInfo;
import com.jackqiu.jackapi.service.InnerInterfaceInfoService;
import com.jackqiu.jackapi.service.InnerUserInterfaceInfoService;
import com.jackqiu.jackapi.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.prefs.BackingStoreException;

@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

//    private static Map<String, InterfaceInfo> interfaceInfoMap;
    private static Map<Long, InterfaceInfo> interfaceInfoMap;

    //TODO 动态转发至不同的服务器
    //思路：网关启动时，获取所有的接口信息，维护到内存的 hashmap 中；
    // 有请求时，根据请求的 url 路径或者其他参数（比如 host 请求头）查询到对应的请求，
    // 来判断应该转发到哪台服务器、以及用于校验接口是否存在
    private static final String INTERFACE_HOST = "http://localhost:8102";//默认路径

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        interfaceInfoMap = innerInterfaceInfoService.getALlInterfaceInfo();
        // 1. 请求日志
        ServerHttpRequest request = exchange.getRequest();
        String path = INTERFACE_HOST + request.getPath().value();
        String method = request.getMethod().toString();
        log.info("请求唯一标识：" + request.getId());
        log.info("请求路径：" + path);
        log.info("请求方法：" + method);
        log.info("请求参数：" + request.getQueryParams());
        String sourceAddress = request.getLocalAddress().getHostString();
        log.info("请求来源地址：" + sourceAddress);
        log.info("请求来源地址：" + request.getRemoteAddress());
        ServerHttpResponse response = exchange.getResponse();
        // 2. 访问控制 - 黑白名单
        if (!IP_WHITE_LIST.contains(sourceAddress)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }
        // 校验参数（网关层面进行过滤）
        HttpHeaders headers = request.getHeaders();
        String body = headers.getFirst("body");
        String assessKey = headers.getFirst("assessKey");
        String sign = headers.getFirst("sign");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        if (body == null) {
            return handleNoAuth(response);
        }
        if (assessKey == null) {
            return handleNoAuth(response);
        }
        if (sign == null) {
            return handleNoAuth(response);
        }
            //随机数的长度校验
        if (Long.parseLong(nonce) > 10000L) {
            return handleNoAuth(response);
        }
            //间隔不能超过5分钟
        long currentTimestamp = System.currentTimeMillis() / 1000;
        Long length = 60 * 5L;
        if (currentTimestamp - Long.parseLong(timestamp) > length) {
            return handleNoAuth(response);
        }
        // 3. 用户鉴权（判断 ak、sk 是否合法）
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(assessKey);
        } catch (Exception e) {
            log.error("getInvokeUser error", e);
        }
        if (invokeUser == null) {
            return handleNoAuth(response);
        }
        //使用加密算法 将secretKey和body一块加密，看sign是否和请求中的一致 (实际情况中是从数据库中查出 secretKey)
        // map.put("sign" , SignUtil.getSign(secretKey, body));
        String secretKey = invokeUser.getSecretKey();
        String nowSign = SignUtil.getSign(secretKey, body);
        if (!nowSign.equals(sign)) {
            return handleNoAuth(response);
        }
        // 4. 请求的模拟接口是否存在，以及请求方法是否匹配
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = innerInterfaceInfoService
                    .getInterface(path, method, request.getQueryParams().toString());
        } catch (Exception e) {
            log.error("getInterface error", e);
        }
        if (interfaceInfo == null) {
            return handleNoAuth(response);
        }
        // 5.是否还有调用次数
        UserInterfaceInfo userInterfaceInfo = null;
        try {
            userInterfaceInfo = innerUserInterfaceInfoService
                    .getUserInterfaceInfo(invokeUser.getId(), interfaceInfo.getId());
        } catch (Exception e) {
            log.error("getUserInterfaceInfo error", e);
        }
        if (userInterfaceInfo == null || userInterfaceInfo.getLeftNum() <= 0) {
            return handleNoAuth(response);
        }
        // 6. 请求转发，调用模拟接口 + 响应日志
        return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());

    }

    /**
     * 处理响应
     *
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // 7. 调用成功，接口调用次数 + 1 invokeCount
                                        try {
                                            innerUserInterfaceInfoService.invokeCountDown(userId, interfaceInfoId);
                                        } catch (Exception e) {
                                            log.error("invokeCount error", e);
                                        }
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);//释放掉内存
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        List<Object> rspArgs = new ArrayList<>();
                                        rspArgs.add(originalResponse.getStatusCode());
                                        String data = new String(content, StandardCharsets.UTF_8); //data
                                        sb2.append(data);
                                        // 打印日志
                                        log.info("响应结果：" + data);
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 8. 报错
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange); // 降级处理返回数据
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}