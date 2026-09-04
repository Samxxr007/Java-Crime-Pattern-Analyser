package com.crimeanalyzer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Thymeleaf page controller — serves HTML template pages.
 *
 * <p>All data is loaded client-side via REST API calls using JavaScript,
 * keeping the server-side templates clean and stateless.
 */
@Controller
public class PageController {

    @GetMapping({"/", "/index"})
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/records")
    public String records() {
        return "records";
    }

    @GetMapping("/map")
    public String map() {
        return "map";
    }

    @GetMapping("/analysis")
    public String analysis() {
        return "analysis";
    }

    @GetMapping("/recommendations")
    public String recommendations() {
        return "recommendations";
    }

    @GetMapping("/compare")
    public String compare() {
        return "compare";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
