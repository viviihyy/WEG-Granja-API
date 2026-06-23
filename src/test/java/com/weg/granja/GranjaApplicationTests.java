package com.weg.granja;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("Smoke test - contexto Spring sobe corretamente")
class GranjaApplicationTests {

    @Test
    @DisplayName("o contexto da aplicação carrega sem erros")
    void contextLoads() {
        // Se o contexto não subir (beans, JPA, configs), este teste falha.
    }
}