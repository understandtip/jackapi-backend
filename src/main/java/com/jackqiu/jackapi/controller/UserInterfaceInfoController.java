package com.jackqiu.jackapi.controller;

import com.jackqiu.jackapi.common.*;
import com.jackqiu.jackapi.exception.BusinessException;
import com.jackqiu.jackapi.exception.ThrowUtils;
import com.jackqiu.jackapi.model.entity.InterfaceInfo;
import com.jackqiu.jackapi.model.entity.User;
import com.jackqiu.jackapi.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/userInterfaceInfo")
@Slf4j
public class UserInterfaceInfoController {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    /**
     * 开通接口
     * @param idRequest id请求对象
     * @param request 请求对象
     * @return
     */
    @PostMapping("/active")
    public BaseResponse<Boolean> activeUserInterfaceInfo(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        //参数校验
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean flag = userInterfaceInfoService.activeUserInterfaceInfo(idRequest, request);
        return ResultUtils.success(flag);
    }
}
