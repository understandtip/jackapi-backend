package com.jackqiu.jackapi.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jackqiu.jackapi.common.ErrorCode;
import com.jackqiu.jackapi.exception.BusinessException;
import com.jackqiu.jackapi.exception.ThrowUtils;
import com.jackqiu.jackapi.model.entity.InterfaceInfo;
import com.jackqiu.jackapi.service.InnerInterfaceInfoService;
import com.jackqiu.jackapi.service.InterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Override
    public InterfaceInfo getInterface(String url, String method, String requestParam) {
        //校验参数
        if (StringUtils.isAnyBlank(url, method, requestParam)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        QueryWrapper<InterfaceInfo> interfaceInfoQueryWrapper = new QueryWrapper<>();
        interfaceInfoQueryWrapper.eq("method", method);
        List<InterfaceInfo> interfaceInfos = interfaceInfoService.list(interfaceInfoQueryWrapper);
        ThrowUtils.throwIf(interfaceInfos == null, ErrorCode.NOT_FOUND_ERROR);
        List<InterfaceInfo> result = interfaceInfos.stream().map(interfaceInfo -> {
            if (url.equals(interfaceInfo.getHost() + interfaceInfo.getUrl())) {
                return interfaceInfo;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public Map<Long, InterfaceInfo> getALlInterfaceInfo() {
        List<InterfaceInfo> interfaceInfos = interfaceInfoService.list();
        HashMap<Long, InterfaceInfo> hashMap = new HashMap<>();
        interfaceInfos.stream().map(interfaceInfo -> {
            hashMap.put(interfaceInfo.getId(), interfaceInfo);
            return null;
        }).collect(Collectors.toList());
        return hashMap;
    }
}
