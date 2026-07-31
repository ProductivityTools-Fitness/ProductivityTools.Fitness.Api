package top.productivitytools.fitness.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
public class DebugController {
    
    @GetMapping("/hello")
    public String Hello() {
        return "Hello";
    }
}
