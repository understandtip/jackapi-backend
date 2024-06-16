package com.jackqiu.jackapi.service;

import com.jackqiu.jackapi.model.entity.User;

/**
 * 用户内部服务
 */
public interface InnerUserService {

    /**
     * 根据assessKey来查询对应的用户
     */
    User getInvokeUser(String assessKey);

}
