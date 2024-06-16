package com.jackqiu.jackapi.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jackqiu.jackapi.common.ErrorCode;
import com.jackqiu.jackapi.exception.BusinessException;
import com.jackqiu.jackapi.model.entity.UserInterfaceInfo;
import com.jackqiu.jackapi.service.InnerUserInterfaceInfoService;
import com.jackqiu.jackapi.service.UserInterfaceInfoService;
import com.jackqiu.jackapi.service.impl.UserInterfaceInfoServiceImpl;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public Boolean invokeCountDown(Long userId, Long interfaceInfoId) {
        return userInterfaceInfoService.invokeCountDown(userId, interfaceInfoId);
    }

    @Override
    public UserInterfaceInfo getUserInterfaceInfo(Long userId, Long interfaceInfoId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (interfaceInfoId == null || interfaceInfoId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("interfaceInfoId", interfaceInfoId);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getOne(queryWrapper);
        return userInterfaceInfo;
    }


}
