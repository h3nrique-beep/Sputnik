package br.edu.fag.sputnik.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Respostas consistentes e sem exposição de detalhes internos da API. */
@RestControllerAdvice(basePackages = "br.edu.fag.sputnik.controller")
public class ApiExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> dadosInvalidos(
            MethodArgumentNotValidException exception) {
        String mensagem =
                exception.getBindingResult().getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .filter(valor -> valor != null && !valor.isBlank())
                        .findFirst()
                        .orElse("Verifique os dados informados.");
        return ResponseEntity.badRequest().body(Map.of("mensagem", mensagem));
    }
}
