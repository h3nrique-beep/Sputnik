package br.edu.fag.sputnik;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:context_test;DB_CLOSE_DELAY=-1",
            "spring.jpa.hibernate.ddl-auto=create-drop"
        })
@ActiveProfiles("test")
class SputnikApplicationTests {
    @Test
    void contextoInicia() {}
}
