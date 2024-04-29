package com.jackqiu.jackapi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jackqiu.jackapi.common.IdRequest;
import com.jackqiu.jackapi.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.jackqiu.jackapi.model.entity.InterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jackqiu.jackapi.model.vo.InterfaceInfoVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author jackqiu
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2024-04-13 13:42:31
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    /**
     * 获取查询条件
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    /**
     * 获取帖子封装
     *
     * @param interfaceInfo
     * @param request
     * @return
     */
    InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param interfaceInfoPage
     * @param request
     * @return
     */
    Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);

    /**
     * 上线接口
     * @param interfaceInfoIdRequest
     * @param request
     * @return
     */
    Boolean onlineInterfaceInfo(IdRequest interfaceInfoIdRequest, HttpServletRequest request);

    /**
     * 下线接口
     * @param interfaceInfoIdRequest
     * @param request
     * @return
     */
    Boolean offlineInterfaceInfo(IdRequest interfaceInfoIdRequest, HttpServletRequest request);
}
