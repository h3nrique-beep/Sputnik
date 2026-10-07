package br.edu.fag.sputnik.controller;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class CamposInternosAdvice {
    @InitBinder
    void protegerIdentidade(WebDataBinder binder) {
        binder.setDisallowedFields("id", "empresaId", "*.empresaId");
    }
}
