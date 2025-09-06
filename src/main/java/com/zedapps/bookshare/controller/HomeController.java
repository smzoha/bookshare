package com.zedapps.bookshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author smzoha
 * @since 6/9/25
 **/
@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    @ResponseBody
    public String getIndex() {
        return "Welcome to Bookshare!";
    }
}
