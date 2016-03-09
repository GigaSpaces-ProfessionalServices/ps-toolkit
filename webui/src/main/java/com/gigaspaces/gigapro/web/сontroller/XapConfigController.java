package com.gigaspaces.gigapro.web.сontroller;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class XapConfigController {

    private static final Logger LOG = LoggerFactory.getLogger(XapConfigController.class);

    @RequestMapping(value = "/generate", method = POST)
    public void generate(@RequestBody XapConfigOptions xapConfigOptions) {
        LOG.info("Generating script using options: \n" + xapConfigOptions);
    }
}
