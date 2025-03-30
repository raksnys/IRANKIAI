package com.irankiai.backend.Test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
public class TestController {
    @GetMapping("/test")
    public String test() {
        return "Hello World!";
    }

    @GetMapping("/test2")
    public String test2() {
        return "TESTAS 123";
    }
    
}
