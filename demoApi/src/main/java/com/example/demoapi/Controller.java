package com.example.demoapi;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class Controller {

    @RequestMapping("/test")
    public String test(String key){
        return key;
    }

    @GetMapping("/test2")
    public String test2(String key,String key2){
        return key+":"+key2;
    }

    @PostMapping("/test3")
    public String test3(@RequestBody DemoRequest key){
        return key.getStr()+":"+key.getStr2();
    }

    @DeleteMapping("/test4")
    public String test4(@RequestBody DemoRequest key){
        return "delete"+key.getStr()+":"+key.getStr2();
    }
}
