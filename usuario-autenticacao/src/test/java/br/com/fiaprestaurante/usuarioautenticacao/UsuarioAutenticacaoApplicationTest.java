package br.com.fiaprestaurante.usuarioautenticacao;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class UsuarioAutenticacaoApplicationTest {

    @Test
    void deveIniciarAplicacaoSpring() {
        String[] args = {"--server.port=0"};

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            UsuarioAutenticacaoApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(UsuarioAutenticacaoApplication.class, args));
        }
    }
}
