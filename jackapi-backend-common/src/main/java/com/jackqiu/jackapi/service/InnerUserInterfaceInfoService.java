package com.jackqiu.jackapi.service;

import com.jackqiu.jackapi.model.entity.UserInterfaceInfo;

/**
 * 用户接口调用关系内部类
 */
public interface InnerUserInterfaceInfoService {
    /**
     * 调用次数减一
     */
    Boolean invokeCountDown(Long userId, Long interfaceInfoId);

    /**
     * 获取调用次数
     */
    UserInterfaceInfo getUserInterfaceInfo(Long userId, Long interfaceInfoId);
}
