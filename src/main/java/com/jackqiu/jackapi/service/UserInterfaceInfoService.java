package com.jackqiu.jackapi.service;

import com.jackqiu.jackapi.common.IdRequest;
import com.jackqiu.jackapi.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author jackqiu
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2024-05-07 21:23:57
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    /**
     * 开通接口
     * @param idRequest id请求对象
     * @param request 请求对象
     * @return
     */
    Boolean activeUserInterfaceInfo(IdRequest idRequest, HttpServletRequest request);

    Boolean invokeCountDown(Long userId, Long interfaceInfoId);
}
