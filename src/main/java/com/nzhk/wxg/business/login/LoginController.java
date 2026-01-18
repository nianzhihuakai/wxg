package com.nzhk.wxg.business.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( "/login")
public class LoginController {

    @PostMapping(value = "/login")
    public String login () {
        return "11111";
    }
}

