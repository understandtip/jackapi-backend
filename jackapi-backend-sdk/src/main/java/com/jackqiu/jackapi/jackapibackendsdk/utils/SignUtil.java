package com.jackqiu.jackapi.jackapibackendsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.crypto.digest.MD5;

public class SignUtil {
    /**
     * 根据密钥   +   请求参数    -->   生成签名
     * @param secretKey
     * @param body
     * @return
     */
    public static String getSign(String secretKey, String body) {
        return new Digester(DigestAlgorithm.SHA256).digestHex(body + secretKey);
    }
}
