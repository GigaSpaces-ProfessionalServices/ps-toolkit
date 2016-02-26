package com.gigaspaces.gigapro.web;

import com.gigaspaces.gigapro.web.listener.BrowserLauncher;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class Webui {
    
    private static final String DEFAULT_URL = "http://localhost:8080";
    
    @RequestMapping("/")
    public String home() {
        return "Gigaspaces XAP script generator";
    }
    
    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(Webui.class)
            .listeners(new BrowserLauncher(DEFAULT_URL))
            .run(args);
    }
}
