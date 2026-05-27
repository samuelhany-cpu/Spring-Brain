package com.example;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MappingMethodController {

    @RequestMapping("/any-endpoint")
    public String anyMethod() {
        return "any";
    }

    @RequestMapping(value = "/post-endpoint", method = RequestMethod.POST)
    public String postMethod() {
        return "post";
    }

    @RequestMapping(value = "/delete-endpoint", method = RequestMethod.DELETE)
    public void deleteMethod() {}

    @RequestMapping(value = "/multi-endpoint", method = {RequestMethod.GET, RequestMethod.POST})
    public String multiMethod() {
        return "multi";
    }

    @RequestMapping(value = "/single-array-endpoint", method = {RequestMethod.PUT})
    public String singleArrayMethod() {
        return "put";
    }
}
