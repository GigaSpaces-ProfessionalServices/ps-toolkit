package com.gigaspaces.gigapro.web;

import com.gigaspaces.gigapro.web.listener.BrowserLauncher;
import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class Webui {
    
    private static final String DEFAULT_URL = "http://localhost:8080";
    
    private static final Logger LOG = LoggerFactory.getLogger(Webui.class);
    
    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public void generate(@RequestBody XapConfigOptions xapConfigOptions) {
        LOG.info("Generating script using options: \n" + xapConfigOptions);
    }
    
    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(Webui.class)
            .listeners(new BrowserLauncher(DEFAULT_URL))
            .run(args);
    }
}
