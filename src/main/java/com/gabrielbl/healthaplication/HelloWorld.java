package com.gabrielbl.healthaplication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/HelloWorld")
public class HelloWorld {
    @GetMapping
    public static String helloworld() {


        return "Hello World!";
    }
}
