package com.jackqiu.jackapi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jackqiu.jackapi.common.ErrorCode;
import com.jackqiu.jackapi.common.IdRequest;
import com.jackqiu.jackapi.constant.CommonConstant;
import com.jackqiu.jackapi.exception.BusinessException;
import com.jackqiu.jackapi.exception.ThrowUtils;
import com.jackqiu.jackapi.jackapibackendsdk.config.APIClient;
import com.jackqiu.jackapi.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.jackqiu.jackapi.model.entity.InterfaceInfo;
import com.jackqiu.jackapi.model.entity.User;
import com.jackqiu.jackapi.model.enums.InterfaceMethodEnum;
import com.jackqiu.jackapi.model.enums.InterfaceStatusEnum;
import com.jackqiu.jackapi.model.vo.InterfaceInfoVO;
import com.jackqiu.jackapi.model.vo.UserVO;
import com.jackqiu.jackapi.service.InterfaceInfoService;
import com.jackqiu.jackapi.mapper.InterfaceInfoMapper;
import com.jackqiu.jackapi.service.UserService;
import com.jackqiu.jackapi.utils.SqlUtils;
import com.jackqiu.jackapi.utils.myJSONUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author jackqiu
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2024-04-13 13:42:31
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService{
    @Resource
    private UserService userService;

    @Value("${api.client.gateway-host}")
    private String GATEWAY_HOST;

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = interfaceInfo.getId();
        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String host = interfaceInfo.getHost();
        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();
        String requestParam = interfaceInfo.getRequestParam();
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        Long userId = interfaceInfo.getUserId();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name, url, method, requestParam), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名字过长");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 2000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述过长");
        }
        if (StringUtils.isNotBlank(host) && host.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "host过长");
        }
        if (StringUtils.isNotBlank(url) && url.length() > 2000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "url过长");
        }
        if (InterfaceMethodEnum.getEnumByValue(method) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求类型错误");
        }
        //必须为json字符串
        if (myJSONUtil.isJSON2(requestHeader) || myJSONUtil.isJSON2(responseHeader)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不为json形式");
        }
        if (userId < 0 || userService.getById(id) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "userId不存在");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (interfaceInfoQueryRequest == null) {
            return queryWrapper;
        }
        Long id = interfaceInfoQueryRequest.getId();
        String name = interfaceInfoQueryRequest.getName();
        String description = interfaceInfoQueryRequest.getDescription();
        String host = interfaceInfoQueryRequest.getHost();
        String url = interfaceInfoQueryRequest.getUrl();
        String method = interfaceInfoQueryRequest.getMethod();
        Integer status = interfaceInfoQueryRequest.getStatus();
        Long userId = interfaceInfoQueryRequest.getUserId();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        // 拼接查询条件
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.and(qw -> qw.like("name", name).or().like("description", description));
        }
        queryWrapper.like(StringUtils.isNotBlank(host), "host", host);
        queryWrapper.like(StringUtils.isNotBlank(url), "url", url);
        queryWrapper.like(StringUtils.isNotBlank(method), "method", method);
        queryWrapper.ne(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request) {
        InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
        BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
        return interfaceInfoVO;
    }

    @Override
    public Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request) {
        List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords();
        Page<InterfaceInfoVO> interfaceInfoVOPage = new Page<>(interfaceInfoPage.getCurrent(), interfaceInfoPage.getSize(),
                interfaceInfoPage.getTotal());
        if (CollUtil.isEmpty(interfaceInfoList)) {
            return interfaceInfoVOPage;
        }
        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoList.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        interfaceInfoVOPage.setRecords(interfaceInfoVOList);
        return interfaceInfoVOPage;
    }

    /**
     * 上线接口
     * @param interfaceInfoIdRequest
     * @param request
     * @return
     */
    @Override
    public Boolean onlineInterfaceInfo(IdRequest interfaceInfoIdRequest, HttpServletRequest request) {
        //校验参数
        if (interfaceInfoIdRequest == null || interfaceInfoIdRequest.getId() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //  b. 查询接口是否存在
        Long interfaceInfoId = interfaceInfoIdRequest.getId();
        InterfaceInfo interfaceInfo = this.getById(interfaceInfoId);
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.PARAMS_ERROR);
        //  c. 判断接口状态是否是未上线
        ThrowUtils.throwIf(InterfaceStatusEnum.ON.getValue().equals(interfaceInfo.getStatus()),
                ErrorCode.PARAMS_ERROR, "该接口已经上线，不能重复上线");
        //  d. 调用接口，看是否正常(通过SDK来进行调用)
        User loginUser = userService.getLoginUser(request);
        String assessKey = loginUser.getAssessKey();
        String secretKey = loginUser.getSecretKey();
        String url = interfaceInfo.getUrl();
        APIClient apiClient = new APIClient(assessKey, secretKey, url, GATEWAY_HOST);
        HttpResponse result = apiClient.getNameByJson(JSONUtil.toJsonStr("jackqiu"));//TODO: 1
        ThrowUtils.throwIf(result.getStatus() != 200, ErrorCode.PARAMS_ERROR, "该接口不能正常调用 或者 请求方式错误");
        //  e. 修改接口状态
        UpdateWrapper<InterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", interfaceInfoId);
        updateWrapper.set("status", 1);
        boolean update = this.update(updateWrapper);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR);
        return update;
    }

    /**
     * 下线接口
     * @param interfaceInfoIdRequest
     * @param request
     * @return
     */
    @Override
    public Boolean offlineInterfaceInfo(IdRequest interfaceInfoIdRequest, HttpServletRequest request) {
        //校验参数
        if (interfaceInfoIdRequest == null || interfaceInfoIdRequest.getId() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //    ⅰ. 判断接口是否存在
        Long interfaceInfoId = interfaceInfoIdRequest.getId();
        InterfaceInfo interfaceInfo = this.getById(interfaceInfoId);
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.SYSTEM_ERROR, "对应数据不存在");
        //    ⅱ. 接口状态需要为在线
        if (interfaceInfo.getStatus() != 1) {
            return true;
        }
        //    ⅲ. 修改接口状态
        interfaceInfo.setStatus(0);
        boolean flag = this.updateById(interfaceInfo);
        ThrowUtils.throwIf(!flag, ErrorCode.SYSTEM_ERROR);
        return flag;
    }
}




