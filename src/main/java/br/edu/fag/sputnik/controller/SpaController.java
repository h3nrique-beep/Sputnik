package br.edu.fag.sputnik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping({"/login", "/sistemas", "/sistemas/**"})
    public String app() {
        return "forward:/index.html";
    }
}
