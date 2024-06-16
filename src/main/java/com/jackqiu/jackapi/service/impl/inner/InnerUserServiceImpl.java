package com.jackqiu.jackapi.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jackqiu.jackapi.common.ErrorCode;
import com.jackqiu.jackapi.exception.BusinessException;
import com.jackqiu.jackapi.exception.ThrowUtils;
import com.jackqiu.jackapi.model.entity.User;
import com.jackqiu.jackapi.service.InnerUserService;
import com.jackqiu.jackapi.service.UserInterfaceInfoService;
import com.jackqiu.jackapi.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    @Override
    public User getInvokeUser(String assessKey) {
        if (StringUtils.isAnyBlank(assessKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        User invokeUser = userService.getOne(new QueryWrapper<User>()
                .eq("assessKey", assessKey));
        ThrowUtils.throwIf(invokeUser == null, ErrorCode.NOT_FOUND_ERROR);
        return invokeUser;
    }
}
