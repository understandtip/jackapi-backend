package com.jackqiu.jackapi.model.dto.interfaceInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口调用请求
 *
 *  
 */
@Data
public class InterfaceInvokeRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 请求参数
     */
    private String param;

    private static final long serialVersionUID = 1L;
}