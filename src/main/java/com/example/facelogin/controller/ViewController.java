/**
 * Copyright (C), 2008-2018, 杭州迪火科技有限公司
 * FileName: ViewController
 * Author:   shugan
 * Date:     2018/3/26 13:29
 * Description: 前端界面
 */
package com.example.facelogin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 〈前端界面〉
 *
 * @author shugan
 * @create 2018/3/26
 * @since 1.0.0
 */
@Controller
@RequestMapping("/")
public class ViewController {
    @RequestMapping("")
    public String index() {
        return "index";
    }
}