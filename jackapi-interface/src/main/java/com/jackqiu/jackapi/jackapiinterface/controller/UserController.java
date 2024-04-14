package com.jackqiu.jackapi.jackapiinterface.controller;

import org.apache.catalina.User;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/name")
@RestController
public class UserController {

    /**
     * get请求
     */
    @GetMapping("/get")
    public String getNameByGet(String name) {
        return "结果是" + name;
    }

    /**
     * post请求
     */
    @PostMapping("/post")
    public String getNameByPost(String name) {
        return "结果是" + name;
    }

    /**
     * json请求
     */
    @PostMapping("/json")
    public String getNameByJson(@RequestBody String name) {
        return "结果是" + name;
    }
}
