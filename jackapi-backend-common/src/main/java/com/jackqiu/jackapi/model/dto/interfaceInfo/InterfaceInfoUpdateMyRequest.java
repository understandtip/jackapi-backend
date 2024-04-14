package com.jackqiu.jackapi.model.dto.interfaceInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新个人信息请求
 *
 *  
 */
@Data
public class InterfaceInfoUpdateMyRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    private static final long serialVersionUID = 1L;
}