package com.jackqiu.jackapi.utils;

import cn.hutool.json.JSONUtil;
import com.jackqiu.jackapi.common.ErrorCode;
import com.jackqiu.jackapi.exception.BusinessException;

/**
 * 判断是否可以被正确解析为json字符串
 */
public class myJSONUtil {
    public static Object toJson(String str) {
        Object obj = null;
        try {
            obj= JSONUtil.parse(str);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不为json形式");
        }
        return obj;
    }

    public static boolean isJSON2(String str) {
        boolean result = false;
        try {
            Object obj=JSONUtil.parse(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }
}
