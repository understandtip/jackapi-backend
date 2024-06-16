package com.jackqiu.jackapi.service;

import com.jackqiu.jackapi.model.entity.InterfaceInfo;

import java.util.List;
import java.util.Map;

/**
 * 接口内部服务
 */
public interface InnerInterfaceInfoService {
    /**
     * 查询对应接口
     */
    InterfaceInfo getInterface(String url, String method, String requestParam);

    Map<Long, InterfaceInfo> getALlInterfaceInfo();
}
