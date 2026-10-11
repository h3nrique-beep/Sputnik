package br.edu.fag.sputnik.controller;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/** Respostas consistentes e sem exposição de detalhes internos da API. */
@RestControllerAdvice(basePackages = "br.edu.fag.sputnik.controller.api")
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> parametroInvalido() {
        return ResponseEntity.badRequest()
                .body(
                        Map.of(
                                "mensagem",
                                "Verifique o identificador e os parâmetros da solicitação."));
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Map<String, String>> alteracaoConcorrente() {
        return ResponseEntity.status(409)
                .body(
                        Map.of(
                                "mensagem",
                                "Outro usuário está alterando este registro. Atualize e tente"
                                        + " novamente."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> regraInvalida(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(
                        Map.of(
                                "mensagem",
                                exception.getMessage() == null
                                        ? "Verifique os dados informados."
                                        : exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido() {
        return ResponseEntity.badRequest()
                .body(Map.of("mensagem", "Verifique o formato e os valores dos dados informados."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> conflito() {
        return ResponseEntity.status(409)
                .body(
                        Map.of(
                                "mensagem",
                                "Os dados conflitam com um cadastro existente ou excedem os limites"
                                        + " permitidos."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
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
