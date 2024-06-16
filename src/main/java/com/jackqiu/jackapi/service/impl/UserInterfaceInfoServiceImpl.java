package com.jackqiu.jackapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jackqiu.jackapi.common.ErrorCode;
import com.jackqiu.jackapi.common.IdRequest;
import com.jackqiu.jackapi.exception.BusinessException;
import com.jackqiu.jackapi.exception.ThrowUtils;
import com.jackqiu.jackapi.model.entity.InterfaceInfo;
import com.jackqiu.jackapi.model.entity.User;
import com.jackqiu.jackapi.model.entity.UserInterfaceInfo;
import com.jackqiu.jackapi.model.enums.InterfaceStatusEnum;
import com.jackqiu.jackapi.service.InterfaceInfoService;
import com.jackqiu.jackapi.service.UserInterfaceInfoService;
import com.jackqiu.jackapi.mapper.UserInterfaceInfoMapper;
import com.jackqiu.jackapi.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author jackqiu
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
* @createDate 2024-05-07 21:23:57
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService{

    @Resource
    private UserService userService;

    @Lazy
    @Resource
    private InterfaceInfoService interfaceInfoService;

    /**
     * 开通接口
     * @param idRequest id请求对象
     * @param request 请求对象
     * @return
     */
    @Override
    public Boolean activeUserInterfaceInfo(IdRequest idRequest, HttpServletRequest request) {
        //参数校验
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //  a. 用户申请开通接口
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        //  b. 判断对应接口状态是否可用
        Long interfaceInfoId = idRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceInfoId);
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        if (InterfaceStatusEnum.OFF.getValue().equals(interfaceInfo.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "对应接口不可用，无法开通");
        }
        //  c. 后端默认添加可调用次数为20的数据到UserInterfaceInfo中
        //前提是不存在对应的数据
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("interfaceInfoId", interfaceInfoId);
        List<UserInterfaceInfo> list = this.list(queryWrapper);
        if (!list.isEmpty()) {//存在对应的数据直接返回
            return true;
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        userInterfaceInfo.setUserId(userId);
        userInterfaceInfo.setInterfaceInfoId(interfaceInfo.getId());
        userInterfaceInfo.setTotalNum(0);
        userInterfaceInfo.setLeftNum(20);
        userInterfaceInfo.setStatus(0);
        boolean save = false;
        synchronized ((userId.toString() + userInterfaceInfo.getInterfaceInfoId().toString()).intern()) {
            save = this.save(userInterfaceInfo);
        }
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR);
        //  d. 返回是否成功的标识
        return save;
    }

    /**
     * 调用次数减一
     */
    @Override
    public Boolean invokeCountDown(Long userId, Long interfaceInfoId) {
        //参数校验
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (interfaceInfoId == null || interfaceInfoId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId", userId);
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        updateWrapper.setSql("totalNum = totalNum + 1, leftNum = leftNum - 1");
        boolean update = this.update(updateWrapper);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "调用失败");
        return update;
    }
}




