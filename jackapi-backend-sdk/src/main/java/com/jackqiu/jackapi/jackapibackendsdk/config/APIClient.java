package com.jackqiu.jackapi.jackapibackendsdk.config;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import cn.hutool.json.JSONUtil;
import com.jackqiu.jackapi.jackapibackendsdk.utils.SignUtil;
import com.jackqiu.jackapi.model.entity.User;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 调用接口的api SDK
 */
public class APIClient {

    //ak
    private String assessKey;

    //sk
    private String secretKey;

    private String url;

    //网关地址
    private String GATEWAY_HOST;

    public APIClient() {
    }

    public APIClient(String assessKey, String secretKey, String url, String GATEWAY_HOST) {
        this.assessKey = assessKey;
        this.secretKey = secretKey;
        this.url = url;
        this.GATEWAY_HOST = GATEWAY_HOST;
    }

    public String getNameByGet(String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        String result = HttpUtil.get(GATEWAY_HOST + url, map);
        System.out.println(result);
        return result;
    }

    public String getNameByPost(String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        String result = HttpUtil.post(GATEWAY_HOST + url, map);
        System.out.println(result);
        return result;
    }

    /**
     * 拼接请求头
     * @param body
     * @return
     */
    public Map<String, String> getRequestMap(String body) {
        HashMap<String, String> map = new HashMap<>();
        //请求参数   用于接收方能够通过body 和双方约定好的secretKey进行签名算法，来校验请求的合法性
        map.put("body", body);
        //assessKey  aK
        map.put("assessKey", assessKey);
        //sign 签名
        map.put("sign" , SignUtil.getSign(secretKey, body));
        //nonce 随机数
        map.put("nonce" , RandomUtil.randomNumbers(4));
        //timestamp 时间戳
        map.put("timestamp" ,String.valueOf(System.currentTimeMillis() / 1000));
        return map;
    }

    public HttpResponse getNameByJson(String name) {
        User user = new User();
        user.setUserName(name);
        String json = JSONUtil.toJsonStr(user);
        System.out.println(json);
        HttpResponse result = HttpRequest.post(GATEWAY_HOST + url)
                        .addHeaders(getRequestMap(json))
                        .body(json)
                        .execute();
        return result;
    }
}
