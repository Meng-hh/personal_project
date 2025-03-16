package com.rookie.personal_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 首页
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("siteName", "我的技术博客");
        return "index"; // 对应 templates/index.html
    }

    // 文件下载接口
}
